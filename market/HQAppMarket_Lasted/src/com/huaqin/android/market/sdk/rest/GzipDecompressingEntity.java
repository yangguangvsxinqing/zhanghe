package com.huaqin.android.market.sdk.rest;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

/**
 * {@link HttpEntityWrapper} for handling gzip Content Coded responses.
 *
 * @since 4.1
 */
public class GzipDecompressingEntity extends DecompressingEntity {

    /**
     * Creates a new {@link GzipDecompressingEntity} which will wrap the specified
     * {@link HttpEntity}.
     *
     * @param entity
     *            the non-null {@link HttpEntity} to be wrapped
     */
    public GzipDecompressingEntity(final HttpEntity entity) {
        super(entity);
    }

    @Override
    InputStream getDecompressingInputStream(final InputStream wrapped) throws IOException {
        return new GZIPInputStream(wrapped);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Header getContentEncoding() {
        /* This HttpEntityWrapper has dealt with the Content-Encoding. */
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getContentLength() {

        /* length of ungzipped content is not known */
        return -1;
    }

}
