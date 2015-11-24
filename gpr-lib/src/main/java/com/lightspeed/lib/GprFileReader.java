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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Range;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.ContiguousSet;
import com.google.common.util.concurrent.ListenableFuture;

import com.lightspeed.gpr.lib.Element;
import com.lightspeed.gpr.lib.AbstractDataInput;

public class GprFileReader extends AbstractDataInput {
    final static int ELEMENT_HEADER_LEN = 6;
    final static int TIMESTAMP_LEN = 7;
    final static byte TYPE_ELEMENT = 0;
    final static byte TYPE_TIMESTAMP = 1;
    final static int CACHE_SIZE = 5000;
    public final static int MAX_PROGRESS = 100;

    final File m_file;
    DataInputStream m_input;

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
                    return null; // todo...
                }
            }
            );

    public GprFileReader(File f) {
	m_file = f;

        try {
            m_input = new DataInputStream(new BufferedInputStream(new FileInputStream(m_file)));
        }
        catch (FileNotFoundException e) {
            // TODO:
        }
        catch (Exception e){
            // TODO:
        }

        m_fileIndexer = new GprFileIndexer();
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
        return true;
    }

    @Override
    public ListenableFuture<Element> getElement(int index) {
        return null;
    }

    @Override
    public String getName() {
        return "TEST";
    }

    @Override
    public int getCurrentIndex() {
        return 0;
    }

    public interface FileIndexProgressListener {
	public void onFileIndexProgress(int progress);
    }

    public class GprFileIndexer implements Runnable {

        int m_progress = 0; // maybe make this atomic...
        Thread m_indexThread;
	FileIndexProgressListener m_progressListener;

        public GprFileIndexer() {
            m_indexThread = new Thread(GprFileIndexer.this);
            m_indexThread.start();
        }

        public boolean isDone() {
            return m_indexThread.isAlive();
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
                        short bytesPerSample = m_input.readShort();
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
		    if(m_progress != (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS)) {
			m_progress = (int)(((double)currOffset/(double)fileLength)*MAX_PROGRESS);
			m_progressListener.onFileIndexProgress(m_progress);
		    }


                }
            }
            catch (EOFException e){
                // TODO:
            }
            catch (Exception e) {
                // TODO:
            }
        }
    }
}
