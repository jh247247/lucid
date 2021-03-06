package com.lightspeed.gpr.lib;

import java.lang.ref.WeakReference;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Range;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ContiguousSet;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.io.BaseEncoding;

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.ThreadPoolHandler;
import com.lightspeed.gpr.lib.AbstractDataInput;

public class GprFileReader extends AbstractDataInput {
    final static int ELEMENT_HEADER_LEN = 6;
    final static int TIMESTAMP_LEN = 7;
    final static byte TYPE_ELEMENT = 0;
    final static byte TYPE_TIMESTAMP = 1;
    final static int CACHE_SIZE = 5000;

    public final static int MAX_PROGRESS = 100;

    final File m_file;
    Lock m_fileLock = new ReentrantLock();
    FileChannel m_input;

    GprFileIndexer m_fileIndexer;

    // each timestamp or element in a file has two common attributes:
    // index in the file
    // total packet length.
    // this class holds them for us so we can do things easier later on.
    private class PacketAttr {
	public PacketAttr(int index) {
	    this.index = index;
	}
	public int index;
	public int length;
    }

    // might not be able to store the entire file as it becomes larger.
    // these store the offset (in bytes) to the start of the element
    private ArrayList<PacketAttr> m_elementIndex = new ArrayList();
    private ArrayList<PacketAttr> m_timestampIndex = new ArrayList();

    public GprFileReader(File f) {
        this(f,null);
    }

    public GprFileReader(File f,
                         FileIndexProgressListener p) {
        m_file = f;
        m_fileIndexer = new GprFileIndexer(p);
    }

    @Override
    public void close() {
        try {
            m_input.close();
        }
        catch(Exception e) {
            // TODO:
        }
    }

    @Override
    public boolean open() {

        try {
            m_input = new FileInputStream(m_file).getChannel();
        }
        catch (FileNotFoundException e) {
            return false;
        }
        catch (Exception e){
            return false;
        }
        return true;
    }

    @Override
    public boolean exists(int index) {
        // wait for indexer to finish...
        // check if element exists in list
        return (m_fileIndexer.isDone() &&
                index >= 0 &&
                index < m_elementIndex.size());
    }

    // this class loads the next CACHE_BATCH_SIZE elements after a given index
    private class ElementGetter implements Callable<Element> {
        DataPacketParser m_parser = new DataPacketParser();
        int m_index;
        public ElementGetter(int index) {
            // find element that isn't loaded, centered around requested
            // index.
            m_index = index;
        }

        public Element call() {
            while(!m_fileIndexer.isDone()) {
                try {
                    Thread.sleep(100);
                }
                catch(Exception e) {
                    // TODO:
                }
            }

            // loading more elements just clogs up the cache...
            // may be a smarter way to do it, but this is good enough for now.
            return loadElement(m_index);
        }

        public Element loadElement(int index) {
            ByteBuffer buf = ByteBuffer.allocate(m_elementIndex.get(index).length);

            try {
                // interpret header...
                int r = m_input.read(buf,m_elementIndex.get(index).index);

                if(r != m_elementIndex.get(index).length) {
                    System.out.println("Header: Read in " + r + "bytes, expected " +
				       m_elementIndex.get(index).length);
                    return null;
                }
            }
            catch (Exception e) {
                System.out.println("Error reading element header: " + e);
                return null;
            }

	    // reposition buffer at start, since read leaves the mark at the end of the buffer.
	    buf.position(0);

	    if(buf.get() != TYPE_ELEMENT) {
		System.out.println("Indexed element " + index + "is not actually an element!");
		return null;
	    }

            try {
                m_parser.parse(buf);
            } catch (Exception ex) {
                System.out.println("Could not read element " + index + " header:" + ex);
            }

            return m_parser.getDecodedElement();
        }
    }

    @Override
    public ListenableFuture<Element> getElement(int index) {

        // submit earlyIndex to executor so we can load in a bunch at once in another thread.
        return ThreadPoolHandler.submit(new ElementGetter(index));


        // TODO: actually read element
        // TODO: cache elements around this element?
    }

    @Override
    public String getName() {
        return "TEST";
    }

    @Override
    public int getCurrentIndex() {
        if(!m_fileIndexer.isDone()) {
            return 0;
        }
        return m_elementIndex.size();
    }

    public interface FileIndexProgressListener {
        public void onFileIndexProgress(int progress);
    }

    // this inner class indexes the file upon instantiation.
    // Element format goes: start(short),stop(short),bytes per
    // sample(byte), (stop-start) samples of bytes per sample length each.
    public class GprFileIndexer implements Runnable {
        DataInputStream m_input;
        int m_progress = 0;
        Thread m_indexThread;
        WeakReference<FileIndexProgressListener> m_progressListener;
        AtomicBoolean m_done = new AtomicBoolean();

        public GprFileIndexer(FileIndexProgressListener p) {
            try {
                m_input = new DataInputStream(new BufferedInputStream(new FileInputStream(m_file)));
            }
            catch (FileNotFoundException e) {
                // TODO:
            }
            catch (Exception e){
                // TODO:
            }

            m_indexThread = new Thread(GprFileIndexer.this);
            m_indexThread.start();
            m_progressListener = new WeakReference<FileIndexProgressListener>(p);
        }

        public boolean isDone() {
            return m_done.get();
        }

        public int getProgress() {
            return m_progress;
        }

        // this implements a faster packet reading state machine that does not process the actual data.
        // as the format gets more complicated this may need to be externalized.
        @Override
        public void run() {
            long fileLength = m_file.length();
            int currOffset = 0;
            byte type;
	    PacketAttr pa = null;
            try {
                while(currOffset < fileLength-1) {
                    type = m_input.readByte();
                    switch(type) {
                    case TYPE_ELEMENT:
                        // add element to list
			pa = new PacketAttr(currOffset);
                        short start = m_input.readShort();
                        short stop = m_input.readShort();
                        byte bytesPerSample = m_input.readByte();

                        int rawSize = (stop-start)*bytesPerSample;
                        int paddedSize = rawSize + ((rawSize%3)!=0 ? (3-(rawSize%3)):0);
                        int b64Size = (paddedSize/3)*4;

                        // System.out.println("Start: " + start);
                        // System.out.println("Stop: " + stop);
                        // System.out.println("bps: " + bytesPerSample);
                        // System.out.println("Data size: " + b64Size);

                        m_input.skip(b64Size);
                        currOffset += ELEMENT_HEADER_LEN + b64Size;
			pa.length = ELEMENT_HEADER_LEN + b64Size;
			m_elementIndex.add(pa);
                        break;

                    case TYPE_TIMESTAMP:
			pa = new PacketAttr(currOffset);
                        currOffset += TIMESTAMP_LEN +1;
                        m_input.skip(TIMESTAMP_LEN);
			pa.length = TIMESTAMP_LEN;
			m_timestampIndex.add(pa);
                        break;
                    default:
                        // idk what this is, skip until we find a valid thing.
                        System.out.println("Invalid type byte read: " + Integer.toHexString(type) + " (" +type+")");
                        break;
                    }
                    if(m_progressListener.get() != null &&
                       m_progress != (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS)) {
                        m_progress = (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS);
                        m_progressListener.get().onFileIndexProgress(m_progress);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error: " + e);

            }
            // have to quit at this point, set the progress to max to
            // dismiss the dialog
            m_progressListener.get().onFileIndexProgress(MAX_PROGRESS);


            try {
                m_input.close();
		open();
            }
            catch(Exception e) {
                // TODO:
            }
            System.out.println("Indexed: " + m_elementIndex.size() + " elements!");
            m_done.lazySet(true);
        }
    }
}
