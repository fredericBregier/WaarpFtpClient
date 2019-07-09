/*******************************************************************************
 * This file is part of Waarp Project (named also Waarp or GG).
 *
 *  Copyright (c) 2019, Waarp SAS, and individual contributors by the @author
 *  tags. See the COPYRIGHT.txt in the distribution for a full listing of
 *  individual contributors.
 *
 *  All Waarp Project is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or (at your
 *  option) any later version.
 *
 *  Waarp is distributed in the hope that it will be useful, but WITHOUT ANY
 *  WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 *  A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with
 *  Waarp . If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

/**
 *
 */
package org.waarp.ftp.client;

import org.waarp.common.logging.WaarpLoggerFactory;
import org.waarp.common.logging.WaarpSlf4JLoggerFactory;
import org.waarp.ftp.client.transaction.Ftp4JClientTransactionTest;
import org.waarp.ftp.client.transaction.FtpClientThread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple test example using predefined scenario (Note: this uses the configuration example for user shutdown command)
 *
 * @author frederic
 *
 */
public class FtpClient {
  public static AtomicLong numberOK = new AtomicLong(0);

  public static AtomicLong numberKO = new AtomicLong(0);

  /**
   * @param args
   */
  public static void main(String[] args) {
    WaarpLoggerFactory.setDefaultFactory(new WaarpSlf4JLoggerFactory(null));
    System.setProperty("javax.net.debug", "false");

    String server = null;
    int port = 21;
    String username = null;
    String passwd = null;
    String account = null;
    String localFilename = null;
    int numberThread = 1;
    int numberIteration = 1;
    if (args.length < 8) {
      System.err.println("Usage: " + FtpClient.class.getSimpleName() +
                         " server port user pwd acct localfilename nbThread nbIter");
      System.exit(1);
    }
    server = args[0];
    port = Integer.parseInt(args[1]);
    username = args[2];
    passwd = args[3];
    account = args[4];
    localFilename = args[5];
    numberThread = Integer.parseInt(args[6]);
    numberIteration = Integer.parseInt(args[7]);
    int type = 0;
    if (args.length > 8) {
      type = Integer.parseInt(args[8]);
    } else {
      System.out.println("Both ways");
    }
    int delay = 0;
    if (args.length > 9) {
      delay = Integer.parseInt(args[9]);
    }
    int isSSL = 0;
    if (args.length > 10) {
      isSSL = Integer.parseInt(args[10]);
    }
    boolean shutdown = false;
    if (args.length > 11) {
      shutdown = Integer.parseInt(args[11]) > 0;
    }
    // initiate Directories
    Ftp4JClientTransactionTest client = new Ftp4JClientTransactionTest(server,
                                                                       port,
                                                                       username,
                                                                       passwd,
                                                                       account,
                                                                       isSSL);
    if (!client.connect()) {
      System.err.println("Cant connect");
      FtpClient.numberKO.incrementAndGet();
      return;
    }
    try {
      for (int i = 0; i < numberThread; i++) {
        client.makeDir("T" + i);
      }
      System.err.println("SITE: " + client.featureEnabled("SITE"));
      System.err.println("SITE CRC: " + client.featureEnabled("SITE XCRC"));
      System.err.println("CRC: " + client.featureEnabled("XCRC"));
      System.err.println("MD5: " + client.featureEnabled("XMD5"));
      System.err.println("SHA1: " + client.featureEnabled("XSHA1"));
    } finally {
      client.logout();
    }
    if (isSSL > 0) {
      try {
        Thread.sleep(100);
      } catch (InterruptedException e) {
      }
    }
    ExecutorService executorService = Executors.newCachedThreadPool();
    long date1 = System.currentTimeMillis();
    for (int i = 0; i < numberThread; i++) {
      executorService.execute(new FtpClientThread("T" + i, server, port,
                                                  username, passwd, account,
                                                  localFilename,
                                                  numberIteration,
                                                  type, delay, isSSL));
      if (delay > 0) {
        try {
          long newdel = ((delay / 3) / 10) * 10;
          if (newdel == 0) {
            Thread.yield();
          } else {
            Thread.sleep(newdel);
          }
        } catch (InterruptedException e) {
        }
      } else {
        Thread.yield();
      }
    }
    try {
      Thread.sleep(100);
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      executorService.shutdownNow();
      Thread.currentThread().interrupt();
    }
    executorService.shutdown();
    long date2 = 0;
    try {
      if (!executorService.awaitTermination(12000, TimeUnit.SECONDS)) {
        date2 = System.currentTimeMillis() - 120000 * 60;
        executorService.shutdownNow();
        if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
          System.err.println("Really not shutdown normally");
        }
      } else {
        date2 = System.currentTimeMillis();
      }
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      executorService.shutdownNow();
      date2 = System.currentTimeMillis();
      Thread.currentThread().interrupt();
    }

    System.out.println(
        localFilename + " " + numberThread + " " + numberIteration + " " +
        type +
        " Real: " + (date2 - date1) + " OK: " +
        numberOK.get() + " KO: " + numberKO.get() + " Trf/s: " +
        (numberOK.get() * 1000 / (date2 - date1)));
    if (shutdown) {
      try {
        Thread.sleep(500);
      } catch (InterruptedException e) {
      }
      client = new Ftp4JClientTransactionTest(server,
                                              port, "fredo", "fred1", "a",
                                              isSSL);
      if (!client.connect()) {
        System.err.println("Cant connect");
        FtpClient.numberKO.incrementAndGet();
        return;
      }
      try {
        String[] results = client.executeSiteCommand("internalshutdown abcdef");
        System.err.print("SHUTDOWN: ");
        for (String string : results) {
          System.err.println(string);
        }
      } finally {
        client.disconnect();
      }
    }
  }

}
