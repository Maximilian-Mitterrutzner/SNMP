package com.mitmax.backend;

/**
 * Static utility class for parsing IPv4 addresses to {@code String} or to binary.
 */
public class AddressHelper {
    private static final int BLOCK_COUNT = 4;

    /**
     * Converts the specified IPv4 address to it's binary representation and returns it.
     * @param address the {@code String} with the IPv4 address to be represented in binary.
     * @return the {@code long} with the binary representation of the specified address
     * @throws IllegalArgumentException if the address is not of valid format for an IPv4 address
     * @throws NumberFormatException if any part of the address is non-numeric
     */
    public static long getAsBinary(String address) {
        long binary = 0;

        String[] parts = address.split("\\.");

        if(parts.length != BLOCK_COUNT) {
            throw new IllegalArgumentException("Incorrect IPv4 format");
        }

        for(int i = 0; i < BLOCK_COUNT; i++) {
            long block = Long.parseLong(parts[i]);

            if(block < 0 || block > 255) {
                throw new IllegalArgumentException("Incorrect IPv4 format");
            }

            binary |= block << (8 * (parts.length - i - 1));
        }

        return binary;
    }

    /**
     * Converts the specified IPv4 address to it's {@code String} representation and returns it.
     * @param binary the {@code long} containing the binary representation of the IPv4 address
     * @return the {@code String} representation of the specified address
     */
    public static String getAsString(long binary) {
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
