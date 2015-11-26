package com.lightspeed.gpr.lib;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

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

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.AbstractDataInput;

public class GprFileReader extends AbstractDataInput {
    final static int ELEMENT_HEADER_LEN = 6;
    final static int TIMESTAMP_LEN = 7;
    final static byte TYPE_ELEMENT = 0;
    final static byte TYPE_TIMESTAMP = 1;
    final static int CACHE_SIZE = 5000;
    // amount of elements to attempt to cache in one go
    final static int CACHE_BATCH_SIZE = 100;

    public final static int MAX_PROGRESS = 100;

    ListeningExecutorService m_executor =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());


    final File m_file;


    GprFileIndexer m_fileIndexer;

    // these store the offset (in bytes) to the start of the element
    private ArrayList<Integer> m_elementIndex = new ArrayList();
    private ArrayList<Integer> m_timestampIndex = new ArrayList(); // TODO: actually use this

    // TODO: preemptive caching of elements around whatever is being accessed
    LoadingCache<Integer, ListenableFuture<Element>> m_elementCache =
        CacheBuilder.newBuilder()
        .maximumSize(CACHE_SIZE)
        .build(new CacheLoader<Integer, ListenableFuture<Element>>() {
                @Override
                public ListenableFuture<Element> load(Integer index) {
                    return getElement(index);
                }
            }
            );

    public GprFileReader(File f) {
        m_file = f;


    }

    public GprFileReader(File f,
                         FileIndexProgressListener p) {
        this(f);
        m_fileIndexer = new GprFileIndexer(p);
    }

    @Override
    public void close() {

    }

    @Override
    public boolean open() {
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
        DataInputStream m_input;

        int m_indexStart;
        int m_index;
        public ElementGetter(int index) {
            // find element that isn't loaded, centered around requested
            // index.
            m_index = index;
            m_indexStart = Math.max(0,index - CACHE_BATCH_SIZE/2);

            try {
                m_input = new DataInputStream(new BufferedInputStream(new FileInputStream(m_file)));
            }
            catch (FileNotFoundException e) {
                // TODO:
            }
            catch (Exception e){
                // TODO:
            }
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

            // go to index and load the next CACHE_BATCH_SIZE elements
            try {
		System.out.println("Skipping to: " + m_elementIndex.get(m_indexStart));
                m_input.skip(m_elementIndex.get(m_indexStart));
            }
            catch (Exception e) {
                // TODO:
            }

	    return loadNextElement();

            // for(int i = 0; i < CACHE_BATCH_SIZE; i++) {
            //     SettableFuture s = SettableFuture.create();
            //     s.set(loadNextElement());
            //     m_elementCache.put(m_index+i, s);
            // }
            // Element ret = null;

            // try {
            //     ret = m_elementCache.get(m_index).get();
            // }
            // catch(Exception e) {
            //     // TODO:
            // }

            // try {
            //     m_input.close();
            // }
            // catch(Exception e) {
            //     // TODO:
            // }

            // return ret;
        }

        public Element loadNextElement() {
            short start;
            short stop;
            byte bps;
            Element ret = null;

            try {
		if(m_input.readByte() != TYPE_ELEMENT){
		    System.out.println("Loading non-element!");
		}
                start = m_input.readShort();
                stop = m_input.readShort();
                bps = m_input.readByte();
		System.out.println("Element stats: " + start+" "+stop+" "+bps);
                ret = new Element(start, stop);
                for(int i = start; i < stop; i++) {
                    // have to have different cases for different data types
                    switch(bps) {
                    case 1:
                        ret.setSample(i,m_input.readByte());
                        break;
                    case 2:
                        ret.setSample(i,m_input.readShort());
                        break;
                    case 4:
                        ret.setSample(i,m_input.readInt());
                        break;
                    case 8:
                        ret.setSample(i,m_input.readDouble());
                        break;
                    default:
                        // wtf..
                        // can't do anything really...
                        // TODO:
                        break;
                    }
                }
            }
            catch(IOException e) {
                // TODO: better log...
                System.out.println("Failed reading element: " + e);
            }

            return ret;
        }
    }

    @Override
    public ListenableFuture<Element> getElement(int index) {

        // submit earlyIndex to executor so we can load in a bunch at once in another thread.
        return m_executor.submit(new ElementGetter(index));


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

    public class GprFileIndexer implements Runnable {
        DataInputStream m_input;
        int m_progress = 0;
        Thread m_indexThread;
        FileIndexProgressListener m_progressListener;
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
            m_progressListener = p;
        }

        public boolean isDone() {
            return m_done.get();
        }

        public int getProgress() {
            return m_progress;
        }

        @Override
        public void run() {
            long fileLength = m_file.length();
            int currOffset = 0;
            byte type;

            try {
                while(currOffset < fileLength) {
                    type = m_input.readByte();
                    switch(type) {
                    case TYPE_ELEMENT:
                        // add element to list
                        m_elementIndex.add(currOffset);
                        short start = m_input.readShort();
                        short stop = m_input.readShort();
                        short bytesPerSample = m_input.readByte();
                        m_input.skip((stop-start)*bytesPerSample);
                        currOffset += ELEMENT_HEADER_LEN +
                            (stop-start)*bytesPerSample;
                        break;
                    case TYPE_TIMESTAMP:
                        m_timestampIndex.add(currOffset);
                        currOffset += TIMESTAMP_LEN +1;
                        m_input.skip(TIMESTAMP_LEN);
                        break;
                    default:
                        // idk what this is, skip until we find a valid thing.
                        break;
                    }
                    if(m_progressListener != null &&
                       m_progress != (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS)) {
                        m_progress = (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS);
                        m_progressListener.onFileIndexProgress(m_progress);
                    }
                }
            }
            catch (Exception e) {
                System.out.println("Error: " + e);
            }

            try {
                m_input.close();
            }
            catch(Exception e) {
                // TODO:
            }
	    System.out.println("Indexed: " + m_elementIndex.size() + " elements!");
            m_done.lazySet(true);
        }
    }
}
