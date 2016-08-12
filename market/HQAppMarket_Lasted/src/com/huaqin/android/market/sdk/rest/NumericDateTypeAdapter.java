package com.huaqin.android.market.sdk.rest;

import java.io.IOException;
import java.util.Date;
import com.repack.google.gson.JsonSyntaxException;
import com.repack.google.gson.internal.bind.MiniGson;
import com.repack.google.gson.internal.bind.TypeAdapter;
import com.repack.google.gson.reflect.TypeToken;
import com.repack.google.gson.stream.JsonReader;
import com.repack.google.gson.stream.JsonToken;
import com.repack.google.gson.stream.JsonWriter;

/**
 * Adapter for Date. Although this class appears stateless, it is not.
 * DateFormat captures its time zone and locale when it is created, which gives
 * this class state. DateFormat isn't thread safe either, so this class has
 * to synchronize its read and write methods.
 */
public final class NumericDateTypeAdapter extends TypeAdapter<Date> {
	
  public static final Factory FACTORY = new Factory() {
    @SuppressWarnings("unchecked") // we use a runtime check to make sure the 'T's equal
    public <T> TypeAdapter<T> create(MiniGson context, TypeToken<T> typeToken) {
      return typeToken.getRawType() == Date.class ? (TypeAdapter<T>) new NumericDateTypeAdapter() : null;
    }
  };

  @Override public Date read(JsonReader reader) throws IOException {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull();
      return null;
    }
    return deserializeToDate(reader.nextString());
  }

  private synchronized Date deserializeToDate(String json) {
    try {
      return new Date(Long.parseLong(json));
    } catch (NumberFormatException e) {
      throw new JsonSyntaxException(json, e);
    }
  }

  @Override public synchronized void write(JsonWriter writer, Date value) throws IOException {
    if (value == null) {
      writer.nullValue();
      return;
    }
    String dateFormatAsString = value.getTime()+"";
    writer.value(dateFormatAsString);
  }
}
