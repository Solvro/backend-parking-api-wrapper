# In-Memory Database Overview
## Location of Serialized Data
The location for serialized data is defined in the application.properties file.  
Base path: /data/statistics.

## InMemoryRepository Interface
A generic interface with two parameters:
K: The key, which must extend Serializable.
V: The value, which must extend Serializable.

Methods Defined in Interface

    void add(K key, V value)
    Adds a new entry to the repository.

    V get(K key)
    Retrieves the value associated with the given key.

    Set<K> fetchAllKeys()
    Fetches all keys stored in the repository.

    Set<Map.Entry<K, V>> fetchAllEntries()
    Fetches all key-value pairs as a set of entries.

## InMemoryRepositoryImpl Class

A concrete implementation of InMemoryRepository with the following structure:  
Fields:

    protected final transient File file
    The file used for storing serialized data.

    protected Map<K, V> dataMap
    The in-memory data map storing all key-value pairs.

    protected V defaultValue
    Default value for the repository (used when no value is found for a key).

Feature:

    Serialization:
    Scheduled serialization of all dataMap values to the file:
        Triggered periodically.
        Ensures data is serialized before the program exits.
    Deserialization:
    Automatically deserializes data from the file when an instance is created.

## Creating a Custom Repository

You can create a custom repository by extending InMemoryRepositoryImpl.  
Steps:

    Create a new class that extends InMemoryRepositoryImpl.
    Use a constructor to configure:
        The file path for serialized data.
        The map type for dataMap.
        The default value for entries (e.g., an empty object or null).

Example

    @Component
    public class ParkingDataRepository extends InMemoryRepositoryImpl<String, ParkingData> {
    
        public ParkingDataRepository(@Value("${custom}") String saveToLocationPath) {
            super(
                saveToLocationPath,      // File path for serialized data
                new HashMap<>(),         // Choose the map type for in-memory storage
                null                     // Default value for entries if there is no key in database
            );
        }
    }