package com.storyous.ksoap2.transport;

import java.io.IOException;
import java.net.Proxy;

/**
 * HttpsTransportSE is a simple transport for https protocal based connections. It creates a #HttpsServiceConnectionSE
 * with the provided parameters.
 *
 * @author Manfred Moser <manfred@simpligility.com>
 */
public class HttpsTransportSE extends HttpTransportSE{

    static final String PROTOCOL = "https";
    private static final String PROTOCOL_FULL = PROTOCOL + "://";
    
    //connection instance, used for setting the SSLSocketFactory
    private HttpsServiceConnectionSE connection;

    protected final String host;
    protected final int port;
    protected final String file;

    public HttpsTransportSE (String host, int port, String file, int timeout) {
        this(host, port, file, timeout, timeout);
    }

    public HttpsTransportSE (String host, int port, String file, int connectTimeout, int readTimeout) {
        super(HttpsTransportSE.PROTOCOL_FULL + host + ":" + port + file, connectTimeout, readTimeout);
        this.host = host;
        this.port = port;
        this.file = file;
    }

    /**
     * Creates instance of HttpTransportSE with set url and defines a
     * proxy server to use to access it
     *
     * @param proxy
     * Proxy information or <code>null</code> for direct access
     */
    public HttpsTransportSE(Proxy proxy, String host, int port, String file, int timeout) {
        this(proxy, host, port, file, timeout, timeout);
    }

    /**
     * Creates instance of HttpTransportSE with set url and defines a
     * proxy server to use to access it
     *
     * @param proxy
     * Proxy information or <code>null</code> for direct access
     */
    public HttpsTransportSE(Proxy proxy, String host, int port, String file, int connectTimeout, int readTimeout) {
        super(proxy, HttpsTransportSE.PROTOCOL_FULL + host + ":" + port + file);
        this.host = host;
        this.port = port;
        this.file = file;
        this.timeout = connectTimeout;
        this.readTimeout = readTimeout;
    }

    /**
     * Returns the HttpsServiceConnectionSE and creates it if necessary
     * @see com.storyous.ksoap2.transport.HttpsTransportSE#getServiceConnection()
     */
    public ServiceConnection getServiceConnection() throws IOException
    {
        if(connection != null) {
            return connection;
        } else {
            connection = new HttpsServiceConnectionSE(proxy, host, port, file, timeout, readTimeout);
            return connection;
        }
    }
}
