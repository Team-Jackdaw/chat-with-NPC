package com.jackdaw.chatwithnpc.auxiliary.yaml;

/**
 * A serializer used to read or write the information from the Yaml files.
 *
 * <p>Read or Write the data file with some information, each file just record one relative information.</p>
 */
public interface YamlMethods {
    /**
     * Read the user data file.
     */
    void sync();


    /**
     * Write the file as the class record.
     */
    void write();

    /**
     * Delete the record.
     */
    void delete();
}
