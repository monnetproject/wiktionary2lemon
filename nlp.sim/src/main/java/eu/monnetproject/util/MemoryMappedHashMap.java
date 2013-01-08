/**********************************************************************************
 * Copyright (c) 2011, Monnet Project
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Monnet Project nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE MONNET PROJECT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *********************************************************************************/
package eu.monnetproject.util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A hash map for handling very, very large objects.
 * 
 * @author John McCrae
 */
public class MemoryMappedHashMap<E, F> extends AbstractMap<E, F> {

    private static int EXTRA_BUF = 0x1000000;
    private static final int BUF_SIZE = 4096;
    private static final int SIZE_OFFSET = 0;
    private static final int TABLE_SIZE_OFFSET = 4;
    private static final int DATA_SIZE_OFFSET = 8;
    private static final int TABLE_OFFSET = 12;
    private int size;
    private final FileChannel channel;
    //private MappedByteBuffer mmap;
    private MappedByteBuffer headerTable;
    private MappedByteBuffer dataEnd;
    private long dataEndOffset;
    private long dataEndEnd;
    private final int hashTableSize;
    private final int dataOffset;
    private int dataSize;
    //private int mapSize;
    private final ByteMapper<E> keyMapper;
    private final ByteMapper<F> valueMapper;

    public MemoryMappedHashMap(File file, int expectedSize, ByteMapper<E> keyMapper, ByteMapper<F> valueMapper) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(file, "rw");
        final long fileLength = raf.length();

        channel = raf.getChannel();

        // First we handle the header, this consists of:
        // First the size (number of entries in the map)
        headerTable = channel.map(MapMode.READ_WRITE, 0, TABLE_OFFSET);
        if (fileLength >= TABLE_SIZE_OFFSET) {
            byte[] buf = new byte[4];
            headerTable.get(buf);
            size = bytesToInt(buf);
        } else {
            size = 0;
            headerTable.put(intToBytes(size));
        }

        // Second the size of the table
        if (fileLength >= DATA_SIZE_OFFSET) {
            byte[] buf = new byte[4];
            headerTable.get(buf);
            hashTableSize = bytesToInt(buf);
        } else {
            hashTableSize = expectedSize;
            byte[] buf = intToBytes(hashTableSize);
            headerTable.put(buf);
        }
        headerTable = channel.map(MapMode.READ_WRITE, 0, TABLE_OFFSET + 4 * hashTableSize);
        headerTable.position(DATA_SIZE_OFFSET);

        // Finally the end of the data block
        if (fileLength >= TABLE_OFFSET) {
            byte[] buf = new byte[4];
            headerTable.get(buf);
            dataSize = bytesToInt(buf);
        } else {
            dataSize = 0;
            byte[] buf = intToBytes(dataSize);
            headerTable.put(buf);
        }
        this.dataOffset = TABLE_OFFSET + 4 * hashTableSize;
        this.keyMapper = keyMapper;
        this.valueMapper = valueMapper;
    }

    private MappedByteBuffer getBuf(long pos, int size) {
        if(dataEnd == null) {
            dataEndOffset = pos;
            dataEndEnd = pos + EXTRA_BUF;
            try {
                return dataEnd = channel.map(MapMode.READ_WRITE, pos, EXTRA_BUF);
            } catch(IOException x) {
                throw new RuntimeException(x);
            }
        } else if(pos > dataEndOffset && pos + size < dataEndEnd) {
            return dataEnd;
        } else {
            dataEndOffset = pos;
            dataEndEnd = pos + EXTRA_BUF;
            try {
                return dataEnd = channel.map(MapMode.READ_WRITE, pos, EXTRA_BUF);
            } catch(IOException x) {
                throw new RuntimeException(x);
            }
            
        }
    }
    
    public static MemoryMappedHashMap<Integer, Integer> getIntIntMap(File file) throws IOException {
        final ByteMapper<Integer> mapper = new ByteMapper<Integer>() {

            @Override
            public byte[] toBytes(Integer g) {
                return intToBytes(g);
            }

            @Override
            public Integer fromBytes(byte[] bytes) {
                return bytesToInt(bytes);
            }

            @Override
            public boolean isFixedSize() {
                return true;
            }
        };

        return new MemoryMappedHashMap<Integer, Integer>(file, EXTRA_BUF, mapper, mapper);
    }

    /*private static int byteToInt(byte b) {
        int i = (byte) b;
        if (Integer.signum(i) > 0) {
            return i;
        } else {
            return i & 0xff;
        }
    }*/

    private static int bytesToInt(byte[] bytes) {
        return ByteBuffer.wrap(bytes).getInt();
        //return (byteToInt(bytes[0]) << 24) + (byteToInt(bytes[1]) << 16) + (byteToInt(bytes[2]) << 8) + byteToInt(bytes[3]);
    }

    private static byte[] intToBytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
        /*return new byte[]{
                    (byte) ((i >> 24) % 256),
                    (byte) ((i >> 16) % 256),
                    (byte) ((i >> 8) % 256),
                    (byte) (i % 256)};*/
    }

    @Override
    public boolean containsKey(Object key) {
        
        try {
            // Calculate the hash code
            final int hashCode = Math.abs(key.hashCode() % hashTableSize);
            // Read the pointer from the hash table
            byte[] buf = new byte[4];
            headerTable.position(TABLE_OFFSET + 4 * hashCode);
            headerTable.get(buf);
            byte[] objAsByte = keyMapper.toBytes((E) key);
            int ptr = bytesToInt(buf);
            if (ptr < dataOffset) {
                return false;
            }
            // Look for collisions
            MMEntry mme = read(ptr);
            while (true) {
                if(Arrays.equals(mme.key, objAsByte)) {
                    return true;
                }
                ptr = mme.next;
                // End of collisions
                if (ptr < dataOffset) {
                    return false;
                }
                mme = read(ptr);
            }
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    
    
    @Override
    public F get(Object key) {

        try {
            // Calculate the hash code
            final int hashCode = Math.abs(key.hashCode() % hashTableSize);
            // Read the pointer from the hash table
            byte[] buf = new byte[4];
            headerTable.position(TABLE_OFFSET + 4 * hashCode);
            headerTable.get(buf);
            byte[] objAsByte = keyMapper.toBytes((E) key);
            int ptr = bytesToInt(buf);
            if (ptr < dataOffset) {
                throw new NoSuchElementException();
            }
            // Look for collisions
            MMEntry mme = read(ptr);
            while (!Arrays.equals(mme.key, objAsByte)) {
                ptr = mme.next;
                // End of collisions
                if (ptr < dataOffset) {
                    throw new NoSuchElementException();
                }
                mme = read(ptr);
            }
            return valueMapper.fromBytes(mme.value);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    private MMEntry read(int ptr) throws IOException {
        byte[] intBuf = new byte[4];
        MappedByteBuffer mmap = channel.map(MapMode.READ_ONLY, ptr, BUF_SIZE);
        mmap.get(intBuf);
        int next = bytesToInt(intBuf);
        mmap.get(intBuf);
        int keyLength = bytesToInt(intBuf);
        byte[] keyBuf = new byte[keyLength];
        mmap.get(keyBuf);
        mmap.get(intBuf);
        int valueLength = bytesToInt(intBuf);
        byte[] valueBuf = new byte[valueLength];
        mmap.get(valueBuf);
        return new MemoryMappedHashMap.MMEntry(next, keyBuf, valueBuf);
    }

    @Override
    public F put(E key, F value) {
        try {
            // Create the new entry
            final MMEntry entry = new MMEntry(0, keyMapper.toBytes(key), valueMapper.toBytes(value));
            final int entrySize = 12 + entry.key.length + entry.value.length;
            // Calculate current value at this hash
            final int hashCode = Math.abs(key.hashCode() % hashTableSize);
            byte[] buf = new byte[4];
            headerTable.position(TABLE_OFFSET + 4 * hashCode);
            headerTable.get(buf);
            int ptr = bytesToInt(buf);
            F rval = null;
            final byte[] entryBytes = entry.toBytes();
            if (ptr < dataOffset) {
                long c = System.currentTimeMillis();
                // This hash is empty
                headerTable.position(TABLE_OFFSET + 4 * hashCode);
                headerTable.put(intToBytes(dataSize + dataOffset));
                MappedByteBuffer dataMap = getBuf(dataSize + dataOffset, entrySize);//channel.map(MapMode.READ_WRITE, dataSize + dataOffset, entrySize);
                dataMap.put(entryBytes);
            } else {
                // There is something at this hash
                //int lastLast = TABLE_OFFSET + 4 * hashCode;
                int last = ptr;
                MMEntry mme = read(ptr);
                // Advance until the end of collisions or equivalent key
                boolean matches = Arrays.equals(mme.key, entryBytes);
                while (mme.next != 0 && !matches) {
                  //  lastLast = last;
                    last = mme.next;
                    mme = read(mme.next);
                    if (mme.next != 0) {
                        matches = Arrays.equals(mme.key, entryBytes);
                        if (matches) {
                            rval = valueMapper.fromBytes(mme.value);
                        }
                    }
                }
                // Note this is functionally the same for main table and collision
                MappedByteBuffer dataMap = getBuf(dataSize + dataOffset, 4 + entryBytes.length);//channel.map(MapMode.READ_WRITE, dataSize + dataOffset, 4 + entryBytes.length);
                headerTable.position(TABLE_OFFSET + 4 * hashCode);
                headerTable.put(intToBytes(dataSize + dataOffset));
                // Copy the next collision into this entry's bytes
                if (matches) {
                    System.arraycopy(intToBytes(mme.next), 0, entryBytes, 0, 4);
                }
                dataMap.put(entryBytes);
            }
            // Increment end of data section
            dataSize += entrySize;
            headerTable.position(DATA_SIZE_OFFSET);
            headerTable.put(intToBytes(dataSize));
            // If the map has changed size, change the size variables
            if (rval == null) {
                size++;
                headerTable.position(SIZE_OFFSET);
                headerTable.put(intToBytes(size));
            }
            return rval;
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public String toString() {
        return "MemoryMappedHashMap of size " + size;
    }

    
    
    @Override
    public F remove(Object key) {
        try {
            // Calculate the hash code
            final int hashCode = Math.abs(key.hashCode() % hashTableSize);
            // Read the pointer from the hash table
            byte[] buf = new byte[4];
            headerTable.position(TABLE_OFFSET + 4 * hashCode);
            headerTable.get(buf);
            byte[] objAsByte = keyMapper.toBytes((E) key);
            int ptr = bytesToInt(buf);
            if (ptr < dataOffset) {
                return null;
            }
            int last = TABLE_OFFSET + 4 * hashCode;
            // Look for collisions
            MMEntry mme = read(ptr);
            while (!Arrays.equals(mme.key, objAsByte)) {
                last = ptr;
                ptr = mme.next;
                // End of collisions
                if (ptr < dataOffset) {
                    return null;
                }
                mme = read(ptr);
            }
            // Overwrite the pointer to mme with next collision (likely 0 for no collision)
            headerTable.position(TABLE_OFFSET + 4 * hashCode);
            headerTable.put(intToBytes(mme.next));
            return valueMapper.fromBytes(mme.value);
        } catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    @Override
    public Set<Entry<E, F>> entrySet() {
        return new MMHMEntrySet();
    }

    public static interface ByteMapper<G> {

        byte[] toBytes(G g);

        G fromBytes(byte[] bytes);

        /**
         * Return true if the array of bytes returned by toBytes is always the same size. Returning the 
         * wrong value will result in data corruption, if in doubt use false.
         * @return 
         */
        boolean isFixedSize();
    }

    public static class StringByteMapper implements ByteMapper<String> {

        @Override
        public String fromBytes(byte[] bytes) {
            return new String(bytes);
        }

        @Override
        public byte[] toBytes(String g) {
            return g.getBytes();
        }

        @Override
        public boolean isFixedSize() {
            return false;
        }
    }

    private class MMHMEntrySet extends AbstractSet<Map.Entry<E, F>> {

        @Override
        public Iterator<Entry<E, F>> iterator() {
            return new MMHMIterator();
        }

        @Override
        public int size() {
            return size;
        }
    }

    private class MMHMIterator implements Iterator<Map.Entry<E, F>> {
        // ptr1 is in the hash table; ptr2 is in the data section

        private int ptr1 = TABLE_OFFSET;
        private int ptr2 = 0;

        public MMHMIterator() {
            advancePtr1();
        }

        private void advancePtr1() {
            byte[] buf = new byte[4];
            for (; ptr1 < TABLE_OFFSET + hashTableSize * 4; ptr1 += 4) {
                //MappedByteBuffer mmap = channel.map(MapMode.READ_WRITE, ptr1, 4);
                headerTable.position(ptr1);
                headerTable.get(buf);
                ptr2 = bytesToInt(buf);
                if (ptr2 != 0) {
                    ptr1 += 4;
                    return;
                }
            }
        }

        private void advancePtr2() {
            try {
                MappedByteBuffer mmap2 = channel.map(MapMode.READ_ONLY, ptr2, 4);
                byte[] buf = new byte[4];
                mmap2.get(buf);
                ptr2 = bytesToInt(buf);
            } catch (IOException x) {
                throw new RuntimeException(x);
            }
        }

        private void advance() {
            if (ptr2 != 0) {
                advancePtr2();
            }
            while (ptr2 == 0 && ptr1 < TABLE_OFFSET + hashTableSize * 4) {
                advancePtr1();
            }
        }

        @Override
        public boolean hasNext() {
            return ptr1 < TABLE_OFFSET + hashTableSize * 4;
        }

        @Override
        public Entry<E, F> next() {
            if (ptr2 == 0) {
                throw new NoSuchElementException();
            } else {
                try {
                    final MMEntry mme = read(ptr2);
                    advance();
                    return new Map.Entry<E, F>() {

                        private final E e = keyMapper.fromBytes(mme.key);
                        private F f = valueMapper.fromBytes(mme.value);

                        @Override
                        public E getKey() {
                            return e;
                        }

                        @Override
                        public F getValue() {
                            return f;
                        }

                        @Override
                        public F setValue(F value) {
                            F oldF = f;
                            f = value;
                            put(e, f);
                            return oldF;
                        }
                    };
                } catch (IOException x) {
                    throw new RuntimeException(x);
                }
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private static class MMEntry {

        public final int next;
        public final byte[] key;
        public final byte[] value;

        public MMEntry(int next, byte[] key, byte[] value) {
            this.next = next;
            this.key = key;
            this.value = value;
        }

        public byte[] toBytes() {
            byte[] bytes = new byte[12 + key.length + value.length];
            System.arraycopy(intToBytes(next), 0, bytes, 0, 4);
            System.arraycopy(intToBytes(key.length), 0, bytes, 4, 4);
            System.arraycopy(key, 0, bytes, 8, key.length);
            System.arraycopy(intToBytes(value.length), 0, bytes, 8 + key.length, 4);
            System.arraycopy(value, 0, bytes, 12 + key.length, value.length);
            return bytes;
        }
    }
}
