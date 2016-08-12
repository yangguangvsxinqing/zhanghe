//package com.fineos.android.rom.sdk.rest;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//import org.apache.http.HttpEntity;
//import org.apache.http.entity.HttpEntityWrapper;
//
///**
// * Common base class for decompressing {@link HttpEntity} implementations.
// * 
// * @since 4.1
// */
//abstract class DecompressingEntity extends HttpEntityWrapper {
//
//	/**
//	 * Default buffer size.
//	 */
//	private static final int BUFFER_SIZE = 1024 * 2;
//
//	/**
//	 * DecompressingEntities are not repeatable, so they will return the same
//	 * InputStream instance when {@link #getContent()} is called.
//	 */
//	private InputStream content;
//
//	/**
//	 * Creates a new {@link DecompressingEntity}.
//	 * 
//	 * @param wrapped
//	 *            the non-null {@link HttpEntity} to be wrapped
//	 */
//	public DecompressingEntity(final HttpEntity wrapped) {
//		super(wrapped);
//	}
//
//	abstract InputStream getDecompressingInputStream(final InputStream wrapped) throws IOException;
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public InputStream getContent() throws IOException {
//		if (wrappedEntity.isStreaming()) {
//			if (content == null) {
//				content = getDecompressingInputStream(wrappedEntity.getContent());
//			}
//			return content;
//		} else {
//			return getDecompressingInputStream(wrappedEntity.getContent());
//		}
//	}
//
//	/**
//	 * {@inheritDoc}
//	 */
//	@Override
//	public void writeTo(OutputStream outstream) throws IOException {
//		if (outstream == null) {
//			throw new IllegalArgumentException("Output stream may not be null");
//		}
//		InputStream instream = getContent();
//		try {
//			byte[] buffer = new byte[BUFFER_SIZE];
//
//			int l;
//
//			while ((l = instream.read(buffer)) != -1) {
//				outstream.write(buffer, 0, l);
//			}
//		} finally {
//			instream.close();
//		}
//	}
//
//}
