package com.ubiqsmart.utils;

import android.content.Context;
import com.ubiqsmart.repository.data.Wallet;

import java.io.*;
import java.util.*;

public class AddressNameConverter {

  private HashMap<String, String> mapdb;
  private static AddressNameConverter instance;

  public static AddressNameConverter getInstance(Context context) {
    if (instance == null) {
      instance = new AddressNameConverter(context);
    }
    return instance;
  }

  private AddressNameConverter(Context context) {
    try {
      load(context);
    } catch (Exception e) {
      mapdb = new HashMap<>();
    }
  }

  public synchronized void put(String address, String name, Context context) {
    if (name == null || name.length() == 0) {
      mapdb.remove(address);
    } else {
      mapdb.put(address, name.length() > 22 ? name.substring(0, 22) : name);
    }
    save(context);
  }

  public String get(String addresse) {
    return mapdb.get(addresse);
  }

  public boolean contains(final String address) {
    return mapdb.containsKey(address);
  }

  public List<Wallet> getAsAddressBook() {
    final List<Wallet> erg = new ArrayList<>();
    for (Map.Entry<String, String> entry : mapdb.entrySet()) {
      erg.add(new Wallet(entry.getValue(), entry.getKey()));
    }
    Collections.sort(erg);
    return erg;
  }

  public synchronized void save(Context context) {
    try {
      FileOutputStream fout = new FileOutputStream(new File(context.getFilesDir(), "namedb.dat"));
      ObjectOutputStream oos = new ObjectOutputStream(fout);
      oos.writeObject(mapdb);
      oos.close();
      fout.close();
    } catch (Exception ignored) {
    }
  }

  @SuppressWarnings("unchecked") public synchronized void load(Context context) throws IOException, ClassNotFoundException {
    final FileInputStream fout = new FileInputStream(new File(context.getFilesDir(), "namedb.dat"));
    final ObjectInputStream oos = new ObjectInputStream(new BufferedInputStream(fout));
    mapdb = (HashMap<String, String>) oos.readObject();
    oos.close();
    fout.close();
  }

}
