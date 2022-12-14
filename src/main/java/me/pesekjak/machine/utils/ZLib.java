package me.pesekjak.machine.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

/**
 * Utility class used for ZLib compression.
 */
@UtilityClass
public class ZLib {

    /**
     * Compresses an array of bytes using ZLib.
     * @param data The array of bytes to compress
     * @return The compressed bytes
     * @throws IOException if an I/O error occurs
     */
    public static byte @NotNull [] compress(byte @NotNull [] data) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final DeflaterOutputStream outputStream = new DeflaterOutputStream(bytes);
        outputStream.write(data);
        outputStream.finish();
        return bytes.toByteArray();
    }

    /**
     * Decompresses a compressed array of bytes.
     * @param data The compressed bytes
     * @return The decompressed bytes
     * @throws IOException if an I/O error occurs
     */
    public static byte @NotNull [] decompress(byte @NotNull [] data) throws IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final InflaterOutputStream outputStream = new InflaterOutputStream(bytes);
        outputStream.write(data);
        outputStream.finish();
        return bytes.toByteArray();
    }

}
