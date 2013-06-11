/*
 * Copyright (C) 2013 Michele Mazzucco
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package http;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import stats.Counter;

// http://www.java2s.com/Code/Java/Network-Protocol/ManagesasynchonousHTTPGETdownloadsanddemonstratesnonblockingIOwithSocketChannelandSelector.htm

/**
 * @(#)file LoadGeneratorAsync.java
 * @(#)author Michele Mazzucco
 * @(#)version 0.1
 * @(#)created Mar 29, 2012
 * @(#)lastedit Mar 29, 2012
 */

/**
 * This is the core class.
 * <p>
 * It uses byte buffers and selectors to concurrently download HTTP traffic from
 * multiple sources in a non-blocking manner.
 */
public class Crawler extends Thread {

	/** For multiplexing non-blocking I/O. */
    private Selector selector;

    /** A shared buffer for downloads. */
    private ByteBuffer buffer;

    /** Downloads that don't have a Channel yet. */
    private List<Download> pendingDownloads;

    /** Set when the release() method is called. */
    private volatile boolean released = false;

    /** Logging output goes here. */
    private final Logger log;

    final Stream stream;

    /** Counter for connect operations. */
    long connections;
    
    /** Counter for write operations. */
    long writes;

    /** The HTTP protocol uses this character encoding. */
    static final Charset LATIN1 = Charset.forName("ISO-8859-1");

    /**
     * Constructor.
     * 
     * @param log The logger.
     * @throws IOException If fails to create the selector object.
     */
    public Crawler(Logger log) throws IOException {
        if (log == null)
            this.log = Logger.getLogger(this.getClass().getName());
        else
            this.log = log;
        selector = Selector.open(); // create Selector
        buffer = ByteBuffer.allocateDirect(64 * 1024); // allocate buffer
        
        // the linked list is more efficient than an array list in this scenario 
        pendingDownloads = Collections
                .synchronizedList(new LinkedList<Download>());

        stream = new Stream();
        connections = 0;
        writes = 0;

        this.start(); // start thread
    }

    /**
     * Ask the HttpDownloadManager to begin a download. Returns a Download
     * object that can be used to poll the progress of the download. The
     * optional Listener object will be notified of when the download completes
     * or aborts.
     */
    public Download download(URI uri, Listener l) {
        if (released)
            throw new IllegalStateException("Can't download() after release()");

        // Get info from the URI
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.equals("http"))
            throw new IllegalArgumentException("Must use 'http:' protocol");
        String hostname = uri.getHost();
        int port = uri.getPort();
        if (port == -1)
            port = 80; // Use default port if none specified
        String path = uri.getRawPath();
        if (path == null || path.length() == 0)
            path = "/";
        String query = uri.getRawQuery();
        if (query != null)
            path += "?" + query;

        if (this.log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, uri.toASCIIString());
        }

        // Create a Download object with the pieces of the URL
        Download download = new DownloadImpl(hostname, port, path, l);

        // Add it to the list of pending downloads. This is a synchronized list
        pendingDownloads.add(download);

        // http://netty.io/docs/stable/xref/org/jboss/netty/channel/socket/nio/NioWorker.html

        // And ask the thread to stop blocking in the select() call so that
        // it will notice and process this new pending Download object.
        // if (wakenUp.compareAndSet(false, true)) {
        // checkPendingDownloads();
        // log.warning("Waking up selector");

        // log.warning("Waking up selector");
        selector.wakeup();
        // }

        // Return the Download so that the caller can monitor it if desired.
        return download;
    }

    /**
     * Terminates this thread and releases the employed resources.
     */
    public void release() {
        released = true; // The thread will terminate when it notices the flag.
        try {
            selector.close();
        } // This will wake the thread up
        catch (IOException e) {
            log.log(Level.SEVERE, "Error closing selector", e);
        }
        this.stream.close();
    }
    
    
    private final int checkPendingDownloads() {
    	int results = 0;
    	while (true) {
    		Download download = null;
    		synchronized (pendingDownloads) {
    			if (pendingDownloads.isEmpty()) {
    				break; // no downloads, exit
    			}
    			
    			// else block
    			download = this.pendingDownloads.remove(0);
			} // end synchronized block
    		results++;
    		
    		// prepare the download outside the synchronized block.
    		prepareDownload(download);
    	}
    	return results;
    }
    
    
    /**
     * Prepares the download.
     */
    private final void prepareDownload(Download download) {
    	// Now begin an asynchronous connection to the
        // specified host and port. We don't block while
        // waiting to connect.
        SelectionKey key = null;
        SocketChannel channel = null;
        try {
            // Open an unconnected channel
            channel = SocketChannel.open();
            // Put it in non-blocking mode
            channel.configureBlocking(false);
            // Register it with the selector, specifying that
            // we want to know when it is ready to connect
            // and when it is ready to read.
            key = channel.register(selector, SelectionKey.OP_READ
                    | SelectionKey.OP_CONNECT
                    | SelectionKey.OP_WRITE, download);
            // Create the web server address
            SocketAddress address = new InetSocketAddress(
                    download.getHost(), download.getPort());
            // Ask the channel to start connecting
            // Note that we don't send the HTTP request yet.
            // We'll do that when the connection completes.
            channel.connect(address);
            channel.socket().setSoTimeout(20000);
            channel.socket().setReuseAddress(true);
        } catch (Exception e) {
            handleError(download, channel, key, e);
        }
    }

    private final void processSelectionKey() {
        // Now get the set of keys that are ready for connecting or reading
        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys == null || keys.isEmpty())
            return; // bug workaround; should not be needed
        // Loop through the keys in the set
        for (Iterator<SelectionKey> i = keys.iterator(); i.hasNext();) {
            SelectionKey key = i.next();
            i.remove(); // Remove the key from the set before handling

            // Get the Download object we attached to the key
            Download download = (Download) key.attachment();
            // Get the channel associated with the key.
            SocketChannel channel = (SocketChannel) key.channel();

            final String host = download.getHost();
            final int port = download.getPort();
            try {

                // ///////////////////////////////////////////////////////////
                // Connect
                // ///////////////////////////////////////////////////////////
                if (key.isConnectable()) {
                    if (channel.isConnectionPending()) {
                        try {
                            // If the channel is ready to connect, complete the
                            // connection and then send the HTTP GET request to
                            // it.
                            if (channel.finishConnect()) {
                                download.setStatus(Status.CONNECTED);
                                this.connections += 1;
                                // need to change the key!!!
                                // this was the bug in the original version
                                // see also here
                                // http://thushw.blogspot.com/2009/11/java-callback-api-for-epoll-building-on.html
                                key.interestOps(SelectionKey.OP_WRITE);
                            } else {
                                this.log.severe("Unable to connect to " + host);
                                try {
                                    key.channel().close();
                                } catch (IOException ignore) {
                                    // ignore
                                }
                                key.cancel();
                                continue;
                            }

                        } catch (ConnectException e) {
                            this.log.severe("Unable to connect to " + host);
                            try {
                                key.channel().close();
                            } catch (IOException ignore) {
                                // ignore
                            }
                            key.cancel();
                            continue;
                        }
                    }
                }

                // ///////////////////////////////////////////////////////////
                // Write
                // ///////////////////////////////////////////////////////////
                if (key.isWritable()) {
                    this.writes += 1;

                    // This is the HTTP request we wend
                    String request = "GET " + download.getPath()
                            + " HTTP/1.1\r\n" + "Host: " + download.getHost()
                            + "\r\n" + "Connection: close\r\n" + "\r\n";
                    // Wrap in a CharBuffer and encode to a
                    // ByteBuffer
                    ByteBuffer requestBytes = LATIN1.encode(CharBuffer
                            .wrap(request));
                    // Send the request to the server. If the bytes
                    // aren't all written in one call, we busy loop!
                    while (requestBytes.hasRemaining())
                        channel.write(requestBytes);

                    if (log.isLoggable(Level.FINEST)) {
                        this.log.finest("Write # " + this.writes
                                + ", sent HTTP request: " + host + ":" + port
                                + ": " + request);
                    }

                    key.interestOps(SelectionKey.OP_READ);
                }

                // ///////////////////////////////////////////////////////////
                // Read
                // ///////////////////////////////////////////////////////////
                if (key.isReadable()) {
                    if (!channel.isOpen()) {
                        throw new IllegalStateException();
                    }

                    // If the key indicates that there is data to be read,
                    // then read it and store it in the Download object.
                    int numbytes = channel.read(buffer);

                    // If we read some bytes, store them, otherwise
                    // the download is complete and we need to note this
                    if (numbytes == 0) {
                        log.warning("Read 0 bytes");
                    } else if (numbytes != -1) {
                        buffer.flip(); // Prepare to drain the buffer
                        // buffer.rewind();
                        download.addData(buffer); // Store the data
                        buffer.clear(); // Prepare for another read
                        if (log.isLoggable(Level.FINEST)) {
                            String msg = String.format(
                                    "Read %d bytes, from %s," + "port %d",
                                    numbytes, host, port);
                            log.finest(msg);
                        }
                    } else {
                        // If there are no more bytes to read
                        key.cancel(); // We're done with the key
                        channel.close(); // And with the channel.
                        download.setStatus(Status.DONE);

                        if (log.isLoggable(Level.CONFIG)) {
                            int received = download.getData().length;
                            int status = download.getHttpStatus();
                            if (status == 200) {
                                String msg = String.format("Received %d bytes",
                                        received);
                                log.log(Level.CONFIG, msg);
                            } else {
                                String msg = String.format("HTTP status %d",
                                        status);
                                log.log(Level.CONFIG, msg);
                            }
                        }

                        // notify listener
                        if (download.getListener() != null)
                            download.getListener().done(download);

                        this.stream.log(download);
                        if (log.isLoggable(Level.FINEST)) {
                            String msg = String
                                    .format("Download completed from %s:%d",
                                            host, port);
                            log.finest(msg);
                        }
                    }
                }

                // http://netty.io/docs/stable/xref/org/jboss/netty/channel/socket/nio/NioWorker.html
                // the key is canceled before receiving the response
                // key.cancel(); // without this command the code enters in
                // infinite loop!!!
            } catch (Exception e) {
                handleError(download, channel, key, e);
            }
        }
        this.selector.selectedKeys().clear();
    }

    public void run() {
        log.info("Crawler thread starting.");

        // final int rebuildThreshold = 10;
        // int consecutiveUselessWakeup = 0;

        // The download thread runs until release() is called
        while (!released) {
            int selected = 0;
            // The thread blocks here waiting for something to happen
            try {
                selected = selector.select();
            } catch (IOException e) {
                // This should never happen.
                log.log(Level.SEVERE, "Error in select()", e);
                break;
            }

            // If release() was called, the thread should exit.
            if (released) {
                break;
            }

            int pending = checkPendingDownloads();
//            if (pending == 0 && selected == 0) {
//                if (log.isLoggable(Level.FINE)) {
//                    log.warning("Idling!!!");
//                }
//                try {
//                    Thread.sleep(10);
//                } catch (InterruptedException e) {
//                    Thread.interrupted();
//                }
//            } else {
            if (pending > 0 || selected > 0)
                processSelectionKey();
//            }
        }
        log.info("Crawler thread exiting.");
    }

    /**
     * Error handling code used by the run() method: 
     * set status, close channel, cancel key, log error, notify listener.
     */
    void handleError(Download download, SocketChannel channel,
            SelectionKey key, Throwable throwable) {
        download.setStatus(Status.ERROR);
        try {
            if (channel != null)
                channel.close();
        } catch (IOException e) {
            //
        }
        if (key != null)
            key.cancel();

        String msg = String.format(
                "Error connecting to or downloading from %s:%d",
                download.getHost(), download.getPort());
        log.log(Level.SEVERE, msg, throwable);
        if (download.getListener() != null)
            download.getListener().error(download, throwable);

        this.stream.logError(download);
    }

    /**
     * Loads the URLs from the specified file.
     * 
     * @param path The file containing the URLs, one per line.
     * @return The list of URLs
     * @throws IOException If an error occurs.
     */
    public static String[] loadUrls(String path) throws IOException {
        List<String> urls = new LinkedList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        String line;
        while ((line = reader.readLine()) != null) {
            urls.add(line);
        }
        reader.close();
        return urls.toArray(new String[urls.size()]);
    }

    private static final int SIZE = 1000000;

    public static void main(String[] args) throws IOException,
            URISyntaxException {

        if (args.length < 2) {
            System.out.println("command <lambda> <file> <[host]>");
            System.exit(1);
        }

        final double lambda = Double.parseDouble(args[0]);
        final String file = args[1];
        double ca2 = 1.0;
        if (args.length > 2) {
            ca2 = Double.parseDouble(args[2]);
        }
        String domain = "http://cloud-proxy.no-ip.org";
        if (args.length == 4) {
            domain = args[3];
        }

        System.out.printf("- Lambda %.2f\n", lambda);
        System.out.printf("- Traces %s\n", file);
        System.out.printf("- ca2 %.2f\n", ca2);
        System.out.printf("- Domain %s\n", domain);

        String[] urls = loadUrls(file);
        final double mean = 1.0 / lambda;

        // initialize everything here, so the CPU will not be wasted
        // afterwards to generate random numbers.

        SecureRandom random = new SecureRandom();
        int i = 0;

        long[] sleep = new long[SIZE];
        String[] randomUrls = new String[SIZE];
        final int urlSize = urls.length;

        double sum = 0.0;
        while (i < SIZE) {
            // exponential distribution
            sleep[i] = (long) (-mean * Math.log(1.0 - random.nextDouble()) * 1000);
            sum += sleep[i];

            randomUrls[i] = urls[random.nextInt(urlSize)];
            i++;
        }

        System.out.printf("Mean sleep %.3f ms\n", (sum / SIZE));

        final Thread main = Thread.currentThread();
        final AtomicBoolean go = new AtomicBoolean(true);

        Logger log = Logger.getAnonymousLogger();
        log.setLevel(Level.FINE);
        final Crawler crawler = new Crawler(log);
        // final LoggerThread logger = new LoggerThread();
        // logger.start();

        // counter for requests
        final AtomicLong requests = new AtomicLong(0);
        // timestamp used to compute arrival rate and throughput
        final long start = System.currentTimeMillis();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                go.set(false);
                main.interrupt();

                crawler.release();
                // logger.done();
                // logger.interrupt();

                final long responses = crawler.stream.getCounter();
                final int errors = crawler.stream.getErrors();
                System.out.println("========================");
                System.out.printf("Requests %d, responses %d, errors %d\n",
                        requests.get(), responses, errors);

                double interval = (System.currentTimeMillis() - start) / 1000.0;

                System.out.printf("Arr rate. %.3f jobs/sec\n",
                        (requests.get() / interval));
                System.out.printf("Throughput. %.3f jobs/sec\n",
                        (responses / interval));

                System.out.println("========================");
                Iterator<Entry<Integer, Counter>> it = crawler.stream.getStatusCodes();
                while (it.hasNext()) {
                    Entry<Integer, Counter> next = it.next();
                    System.out.printf("HTTP code %d: %d\n", next.getKey(),
                            next.getValue().getCounter());
                }
                System.out.println("========================");

            }
        });

        log.info("Starting while loop");
        int counter = 0;
        while (go.get()) {
            String myURL = domain + randomUrls[counter];
            URL url = new URL(myURL);
            URI uri = new URI(url.getProtocol(), url.getHost(), url.getPath(),
                    url.getQuery(), null);
            crawler.download(uri, null);
            requests.incrementAndGet();

            try {
                Thread.sleep(sleep[counter]);
            } catch (InterruptedException e) {
                Thread.interrupted(); // clear interrupted status
            }

            counter++;
            if (counter == randomUrls.length)
                counter = 0;
        }

    }

}
