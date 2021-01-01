package com.mitmax.backend;

public class AddressHelper {
    private static final int BLOCK_COUNT = 4;

    static long getAsBinary(String address) {
        long binary = 0;

        String[] parts = address.split("\\.");
        for(int i = 0; i < BLOCK_COUNT; i++) {
            binary |= Long.parseLong(parts[i]) << (8 * (parts.length - i - 1));
        }

        return binary;
    }

    static String getAsString(long binary) {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < BLOCK_COUNT; i++) {
            long block;

            block = binary & 255;
            binary >>= 8;

            builder.insert(0, block);
            builder.insert(0, '.');
        }

        builder.deleteCharAt(0);
        return builder.toString();
    }
}
