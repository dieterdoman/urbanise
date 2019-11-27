# Question 1
## A:

I would store both the data of XYZ and ABC in couchDB instances, and have serialization on each side to map the central data of the couchDB instance and serialize it in way the each system understands, I would then use couchDB live replication between the 2 systems such that it does the syncing for you with its live replication.

PouchDB and CouchDB were designed for one main purpose: sync.

[CouchDB Live replication](https://pouchdb.com/guides/replication.html#live%E2%80%93replication)

## B:

Since you are reallying on couchDB for live replication, you can mostly run test for the serialization on each side, with a mockDB.

If you still want to test it end to end you can perform CRUD actions on ABC and see if they replicate to XYZ and the other way around. Also should perform them at the same time to see conflict handling.

## C - D Listed problems and how to handle them:

### One of the instances goes offline

What if the user goes offline? In those cases, an error will be thrown and replication will stop.

You can allow PouchDB to automatically handle this error, and retry until the connection is re-established, by using the retry option.

### Conflicts

There might be conflicting changes with the couchDB instances:

Immediate conflicts can occur with any API that takes a rev or a document with _rev as input – put(), post(), remove(), bulkDocs(), and putAttachment(). They manifest as a 409 (conflict) error and in your code, you should always be handling conflicts.

For instance, if you are doing live replication, a document may be modified by somebody else while the user is working on it. If the remote changes are replicated to the local database before the user tries to commit their changes, then they will receive the above 409 error. Possible solutions:

#### Upsert
In many cases, the most practical solution to the 409 problem is to retry the put() until it succeeds. If the user's intended change can be expressed as a delta (i.e. a change that doesn't depend on the current revision), then this is very easy to achieve.

Borrowing a phrase from traditional databases, let's call this an upsert ("update or insert"), and use the pouchdb-upsert plugin.

#### Eventual conflicts
Now, let's move on to the second type: eventual conflicts.

Imagine two PouchDB databases have both gone offline. The two separate users each make modifications to the same document, and then they come back online at a later time.

Both users committed changes to the same version of the document, and their local databases did not throw 409 errors. What happens then?

This is the classic "conflict" scenario, and CouchDB handles it very elegantly. By default, CouchDB will choose an arbitrary winner based on a deterministic algorithm, which means both users will see the same winner once they're back online. However, since the replication history is stored, you can always go back in time to resolve the conflict.

To detect if a document is in conflict, you use the {conflicts: true} option when you get() it.

Both databases will see the same conflict, assuming replication has completed. In fact, all databases in the network will see the exact same revision history – much like Git.

Source: [CouchDB Conflict resolution](https://pouchdb.com/guides/conflicts.html)

## E:

Scaling with couchDB:

### Scaling Read Requests
A read request retrieves a piece of information from the database. It passes the following stations within CouchDB. First, the HTTP server module needs to accept the request. For that, it opens a socket to send data over. The next station is the HTTP request handle module that analyzes the request and directs it to the appropriate submodule in CouchDB. For single documents, the request then gets passed to the database module where the data for the document is looked up on the filesystem and returned all the way up again.

All this takes processing time and enough sockets (or file descriptors) must be available. The storage backend of the server must be able to fulfill all read requests. There are a few more things that can limit a system to accept more read requests; the basic point here is that a single server can process only so many concurrent requests. If your applications generate more requests, you need to set up a second server that your application can read from.

The nice thing about read requests is that they can be cached. Often-used items can be held in memory and can be returned at a much higher level than the one that is your bottleneck. Requests that can use this cache don’t ever hit your database and are thus virtually toll-free.

### Scaling Write Requests
A write request is like a read request, only a little worse. It not only reads a piece of data from disk, it writes it back after modifying it. Remember, the nice thing about reads is that they’re cacheable. Writes: not so much. A cache must be notified when a write changes data, or clients must be told to not use the cache. If you have multiple servers for scaling reads, a write must occur on all servers. In any case, you need to work harder with a write.

### Scaling Data
The third way of scaling is scaling data. Today’s hard drives are cheap and have a lot of capacity, and they will only get better in the future, but there is only so much data a single server can make sensible use of. It must maintain one more indexes to the data that uses disk space again. Creating backups will take longer and other maintenance tasks become a pain.

The solution is to chop the data into manageable chunks and put each chunk on a separate server. All servers with a chunk now form a cluster that holds all your data.

While we are taking separate looks at scaling of reads, writes, and data, these rarely occur isolated. Decisions to scale one will affect the others.

### Potential serialization performance problem

If the serialization gets to large to convert the couchDB documents to how ABC or XYZ uses it, it can be potentially slow.

# Question 2

Question to is implemented in this repo. It has a test to see if it calculates the transient dependencies correctly.