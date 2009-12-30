package pl.pronux.sokker.downloader;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.SocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import pl.pronux.sokker.interfaces.IProgressMonitor;
import pl.pronux.sokker.model.Cookie;
import pl.pronux.sokker.model.ProxySettings;
import pl.pronux.sokker.resources.Messages;
import pl.pronux.sokker.utils.security.Base64Coder;

public class HTMLDownloader {

	private String cookies = ""; //$NON-NLS-1$
	private Proxy proxy;
	private String proxyAuth;

	public HTMLDownloader(ProxySettings proxySettings) {
		if (proxySettings.isEnabled()) {
			init(proxySettings.getHostname(), proxySettings.getPort(), proxySettings.getUsername(), proxySettings.getPassword());
		} else {
			init(null, 0, null, null);
		}
	}

	private void init(String proxyHost, Integer proxyPort, String proxyUser, String proxyPass) {
		if ((proxyHost != null) && (proxyHost.length() > 0) && (proxyPort != null) && (proxyPort.intValue() > 0)) {
			SocketAddress address = new InetSocketAddress(proxyHost, proxyPort.intValue());
			this.proxy = new Proxy(Proxy.Type.HTTP, address);
		} else {
			this.proxy = Proxy.NO_PROXY;
		}

		if ((proxyUser != null) && (proxyUser.length() > 0) && (proxyPass != null) && (proxyPass.length() > 0)) {
			final String pw = proxyUser + ":" + proxyPass; //$NON-NLS-1$
			this.proxyAuth = Base64Coder.encodeString(pw);
		}
	}
	
	public void downloadPackage(final String srcFile, String dstDirectory, String dstFile, IProgressMonitor monitor) throws IOException {
		int length;
		int counter;
		URL url = null;
		try {
			url = new URL(srcFile);
			URLConnection con ;
			if (Proxy.NO_PROXY.equals(this.proxy)) {
				con = (HttpURLConnection) url.openConnection();
			} else {
				con = (HttpURLConnection) url.openConnection(this.proxy);
			}
			length = con.getContentLength();
		} catch (MalformedURLException e1) {
			length = -1;
		} catch (IOException e) {
			length = -1;
		}

		if (url == null) {
			throw new IOException(Messages.getString("exception.url.null")); //$NON-NLS-1$
		}

		try {
			File file = new File(dstDirectory);
			if (!file.exists()) {
				file.mkdirs();
			}
			byte[] buf = new byte[4096];
			int len;

			BufferedInputStream in = new BufferedInputStream(url.openStream());
			FileOutputStream out = new FileOutputStream(dstDirectory + File.separator + dstFile);
			counter = 0;
			while ((len = in.read(buf)) > 0) {
				counter = counter + (len);
				out.write(buf, 0, len);
				if (length != -1) {
					monitor.subTask(String.format("%s ( %dkb of %dkb )", srcFile, counter / 1000, length / 1000)); //$NON-NLS-1$
				} else {
					monitor.subTask(String.format("%s ( %dkb)", srcFile, counter / 1000)); //$NON-NLS-1$
				}
			}
			in.close();
			out.close();
		} catch (final IOException e) {
			throw e;
		}
	}

	public String getPageInBytes(String urlString) throws IOException {
		URL url;
		int len;
		BufferedInputStream in = null;
		HttpURLConnection connection = null;
		StringBuilder buffer = new StringBuilder();
		try {
			url = new URL(urlString);

			if (Proxy.NO_PROXY.equals(this.proxy)) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(this.proxy);
			}

			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8) Gecko/20051224 Debian/1.5.dfsg-3 Firefox/1.5"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"); //$NON-NLS-1$ //$NON-NLS-2$
			// connection.setRequestProperty("Accept-Language", "en");
			// connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.7"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Keep-Alive", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.proxyAuth != null) {
				connection.setRequestProperty("Proxy-Authorization", "Basic " + this.proxyAuth); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// for first request cookie doesn't exist
			if (!cookies.isEmpty()) { //$NON-NLS-1$
				connection.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			} else {
				cookies = getPHPSESSIONID(connection);
			}

			in = new BufferedInputStream(new URL(urlString).openStream());
			while ((len = in.read()) > 0) {
				buffer.append((char) len);
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

		return buffer.toString();
	}

	public String getNormalPage(String urlString) throws IOException {
		StringBuilder content = new StringBuilder();
		String line;
		BufferedReader in = null;
		HttpURLConnection connection = null;
		URL url;
		try {
			url = new URL(urlString);

			if (Proxy.NO_PROXY.equals(this.proxy)) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(this.proxy);
			}

			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8) Gecko/20051224 Debian/1.5.dfsg-3 Firefox/1.5"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"); //$NON-NLS-1$ //$NON-NLS-2$
			// connection.setRequestProperty("Accept-Language", "en");
			// connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Accept-Charset", "utf-8;q=0.7,*;q=0.7"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Keep-Alive", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.proxyAuth != null) {
				connection.setRequestProperty("Proxy-Authorization", "Basic " + this.proxyAuth); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// for first request cookie doesn't exist
			if (!cookies.isEmpty()) { //$NON-NLS-1$
				connection.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			} else {
				cookies = getPHPSESSIONID(connection);
			}

			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			while ((line = in.readLine()) != null) {
				content.append(line).append("\n"); //$NON-NLS-1$
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return content.toString();
	}

	public void getInternetFile(String urlString, String filename, String destinationDirectory) throws IOException {
		byte[] buf = new byte[4096];
		int len;
		BufferedInputStream in = null;
		FileOutputStream out = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(urlString);
			if (Proxy.NO_PROXY.equals(this.proxy)) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(this.proxy);
			}
			in = new BufferedInputStream(connection.getInputStream());
			out = new FileOutputStream(destinationDirectory + File.separator + filename);
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}

		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	public byte[] getInternetFile(String urlString) throws IOException {
		BufferedInputStream in = null;
		HttpURLConnection connection = null;
		try {

			byte[] buf = new byte[4096];
			int len;

			ByteBuffer byteBuffer = ByteBuffer.wrap(buf);

			URL url = new URL(urlString);
			if (Proxy.NO_PROXY.equals(this.proxy)) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(this.proxy);
			}

			in = new BufferedInputStream(connection.getInputStream());
			while ((len = in.read(buf)) > 0) {
				byteBuffer.put(buf, 0, len);
			}

			return byteBuffer.array();
		} finally {
			if (in != null) {
				in.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
	}

	private String getPHPSESSIONID(HttpURLConnection conn) {
		Cookie cookies = new Cookie();
		StringBuilder cookie = new StringBuilder();
		for (int i = 0;; i++) {
			String headerName = conn.getHeaderFieldKey(i);
			String headerValue = conn.getHeaderField(i);

			if (headerName == null && headerValue == null) {
				// No more headers
				break;
			}
			if ("Set-Cookie".equalsIgnoreCase(headerName)) { //$NON-NLS-1$
				// Parse cookie
				String[] fields = headerValue.split(";\\s*"); //$NON-NLS-1$

				String cookieValue = fields[0];
				String expires = null;
				String path = null;
				String domain = null;
				boolean secure = false;

				// Parse each field
				for (int j = 1; j < fields.length; j++) {
					if ("secure".equalsIgnoreCase(fields[j])) { //$NON-NLS-1$
						secure = true;
					} else if (fields[j].indexOf('=') > 0) {
						String[] f = fields[j].split("="); //$NON-NLS-1$
						if ("expires".equalsIgnoreCase(f[0])) { //$NON-NLS-1$
							expires = f[1];
						} else if ("domain".equalsIgnoreCase(f[0])) { //$NON-NLS-1$
							domain = f[1];
						} else if ("path".equalsIgnoreCase(f[0])) { //$NON-NLS-1$
							path = f[1];
						}
					}
				}

				// Save the cookie...

				cookie.append(cookieValue).append(";"); //$NON-NLS-1$
				cookies.setCookieValue(cookieValue);
				cookies.setDomain(domain);
				cookies.setExpires(expires);
				cookies.setPath(path);
				cookies.setSecure(secure);
			}
		}
		return cookie.toString();
	}

	public String postDataToPage(String urlString, String parameters, String referer) throws IOException {
		String line;
		StringBuilder content = new StringBuilder();
		URL url = new URL(urlString);
		HttpURLConnection connection = null;
		BufferedReader in = null;
		DataOutputStream out = null;
		try {
			if (Proxy.NO_PROXY.equals(this.proxy)) {
				connection = (HttpURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection(this.proxy);
			}

			connection.setRequestMethod("POST"); //$NON-NLS-1$
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8) Gecko/20051224 Debian/1.5.dfsg-3 Firefox/1.5"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Accept", "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Accept-Language", "pl"); //$NON-NLS-1$ //$NON-NLS-2$
			// connection.setRequestProperty("Accept-Encoding", "gzip,deflate");
			connection.setRequestProperty("Accept-Charset", "UTF-8,*"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Keep-Alive", "300"); //$NON-NLS-1$ //$NON-NLS-2$
			connection.setRequestProperty("Referer", referer); //$NON-NLS-1$
			connection.setRequestProperty("Cookie", cookies); //$NON-NLS-1$
			connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded"); //$NON-NLS-1$ //$NON-NLS-2$
			if (this.proxyAuth != null) {
				connection.setRequestProperty("Proxy-Authorization", "Basic " + this.proxyAuth); //$NON-NLS-1$ //$NON-NLS-2$
			}
			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			// helping with loggin into the page
			// connection.setInstanceFollowRedirects(false);

			// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

			connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.setUseCaches(true);
			connection.connect();

			out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(parameters);
			out.flush();

			in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8")); //$NON-NLS-1$

			while ((line = in.readLine()) != null) {
				content.append(line.replaceAll("&", "&amp;")).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
				// stringCache = stringCache.replaceAll("<", "&lt;");
				// stringCache = stringCache.replaceAll(">", "&gt;");
				// stringCache = stringCache.replaceAll("\"", "&quot;");
				// stringCache = stringCache.replaceAll("'", "&apos;");
			}

		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
			if (connection != null) {
				connection.disconnect();
			}
		}
		return content.toString();
	}
}