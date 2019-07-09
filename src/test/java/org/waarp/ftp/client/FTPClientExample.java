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
package org.waarp.ftp.client;

import org.apache.commons.net.PrintCommandListener;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPHTTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.commons.net.util.TrustManagerUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * This is the FTPClient example from Apache Commons-Net<br>
 * <p>
 * This is an example program demonstrating how to use the FTPClient class. This
 * program connects to an FTP server and
 * retrieves the specified file. If the -s flag is used, it stores the local
 * file at the FTP server. Just so you can see
 * what's happening, all reply strings are printed. If the -b flag is used, a
 * binary transfer is assumed (default is
 * ASCII). See below for further options.
 */
public final class FTPClientExample {

  public static final String USAGE =
      "Usage: ftp [options] <hostname> <username> <password> <account> [<remote file> [<local file>]]\n"
      +
      "\nDefault behavior is to download a file and use ASCII transfer mode.\n"
      +
      "\t-a - use local active mode (default is local passive)\n"
      +
      "\t-b - use binary transfer mode\n"
      +
      "\t-c cmd - issue arbitrary command (remote is used as a parameter if provided) \n"
      +
      "\t-d - list directory details using MLSD (remote is used as the pathname if provided)\n"
      +
      "\t-e - use EPSV with IPv4 (default false)\n"
      +
      "\t-f - issue FEAT command (remote and local files are ignored)\n"
      +
      "\t-h - list hidden files (applies to -l and -n only)\n"
      +
      "\t-k secs - use keep-alive timer (setControlKeepAliveTimeout)\n"
      +
      "\t-l - list files using LIST (remote is used as the pathname if provided)\n"
      +
      "\t-L - use lenient future dates (server dates may be up to 1 day into future)\n"
      +
      "\t-n - list file names using NLST (remote is used as the pathname if provided)\n"
      +
      "\t-p true|false|protocol[,true|false] - use FTPSClient with the specified protocol and/or isImplicit setting\n"
      +
      "\t-s - store file on server (upload)\n"
      +
      "\t-t - list file details using MLST (remote is used as the pathname if provided)\n"
      +
      "\t-w msec - wait time for keep-alive reply (setControlKeepAliveReplyTimeout)\n"
      +
      "\t-T  all|valid|none - use one of the built-in TrustManager implementations (none = JVM default)\n"
      +
      "\t-PrH server[:port] - HTTP Proxy host and optional port[80] \n" +
      "\t-PrU user - HTTP Proxy server username\n" +
      "\t-PrP password - HTTP Proxy server password\n" +
      "\t-# - add hash display during transfers\n";

  public static final void main(String[] args) {
    boolean storeFile = false, binaryTransfer = false, error = false,
        listFiles = false, listNames = false, hidden =
        false;
    boolean localActive = false, useEpsvWithIPv4 = false, feat = false,
        printHash = false;
    boolean mlst = false, mlsd = false;
    boolean lenient = false;
    long keepAliveTimeout = -1;
    int controlKeepAliveReplyTimeout = -1;
    int minParams = 5; // listings require 3 params
    String protocol = null; // SSL protocol
    String doCommand = null;
    String trustmgr = null;
    String proxyHost = null;
    int proxyPort = 80;
    String proxyUser = null;
    String proxyPassword = null;

    int base = 0;
    for (base = 0; base < args.length; base++) {
      if (args[base].equals("-s")) {
        storeFile = true;
      } else if (args[base].equals("-a")) {
        localActive = true;
      } else if (args[base].equals("-b")) {
        binaryTransfer = true;
      } else if (args[base].equals("-c")) {
        doCommand = args[++base];
        minParams = 3;
      } else if (args[base].equals("-d")) {
        mlsd = true;
        minParams = 3;
      } else if (args[base].equals("-e")) {
        useEpsvWithIPv4 = true;
      } else if (args[base].equals("-f")) {
        feat = true;
        minParams = 3;
      } else if (args[base].equals("-h")) {
        hidden = true;
      } else if (args[base].equals("-k")) {
        keepAliveTimeout = Long.parseLong(args[++base]);
      } else if (args[base].equals("-l")) {
        listFiles = true;
        minParams = 3;
      } else if (args[base].equals("-L")) {
        lenient = true;
      } else if (args[base].equals("-n")) {
        listNames = true;
        minParams = 3;
      } else if (args[base].equals("-p")) {
        protocol = args[++base];
      } else if (args[base].equals("-t")) {
        mlst = true;
        minParams = 3;
      } else if (args[base].equals("-w")) {
        controlKeepAliveReplyTimeout = Integer.parseInt(args[++base]);
      } else if (args[base].equals("-T")) {
        trustmgr = args[++base];
      } else if (args[base].equals("-PrH")) {
        proxyHost = args[++base];
        String parts[] = proxyHost.split(":");
        if (parts.length == 2) {
          proxyHost = parts[0];
          proxyPort = Integer.parseInt(parts[1]);
        }
      } else if (args[base].equals("-PrU")) {
        proxyUser = args[++base];
      } else if (args[base].equals("-PrP")) {
        proxyPassword = args[++base];
      } else if (args[base].equals("-#")) {
        printHash = true;
      } else {
        break;
      }
    }

    int remain = args.length - base;
    if (remain < minParams) // server, user, pass, remote, local [protocol]
    {
      System.err.println(USAGE);
      System.exit(1);
    }

    String server = args[base++];
    int port = 0;
    String parts[] = server.split(":");
    if (parts.length == 2) {
      server = parts[0];
      port = Integer.parseInt(parts[1]);
    }
    String username = args[base++];
    String password = args[base++];
    String account = args[base++];

    String remote = null;
    if (args.length - base > 0) {
      remote = args[base++];
    }

    String local = null;
    if (args.length - base > 0) {
      local = args[base++];
    }

    boolean mustCallProtP = false;
    final FTPClient ftp;
    if (protocol == null) {
      if (proxyHost != null) {
        System.out.println("Using HTTP proxy server: " + proxyHost);
        ftp = new FTPHTTPClient(proxyHost, proxyPort, proxyUser, proxyPassword);
      } else {
        ftp = new FTPClient();
      }
    } else {
      FTPSClient ftps;
      if (protocol.equals("true")) {
        System.out.println("Implicit FTPS");
        ftps = new FTPSClient(true);
      } else if (protocol.equals("false")) {
        System.out.println("Explicit FTPS");
        ftps = new FTPSClient(false);
        mustCallProtP = true;
      } else {
        String prot[] = protocol.split(",");
        System.out.println("Protocl FTPS: " + protocol);

        if (prot.length == 1) { // Just protocol
          ftps = new FTPSClient(protocol);
        } else { // protocol,true|false
          mustCallProtP = !Boolean.parseBoolean(prot[1]);
          ftps = new FTPSClient(prot[0], !mustCallProtP);
        }
      }
      ftp = ftps;
      if ("all".equals(trustmgr)) {
        System.out.println("Accept all");
        ftps.setTrustManager(TrustManagerUtils.getAcceptAllTrustManager());
      } else if ("valid".equals(trustmgr)) {
        System.out.println("Accept after valid");
        ftps.setTrustManager(
            TrustManagerUtils.getValidateServerCertificateTrustManager());
      } else if ("none".equals(trustmgr)) {
        System.out.println("Accept none");
        ftps.setTrustManager(null);
      }
    }

    if (printHash) {
      ftp.setCopyStreamListener(createListener());
    }
    if (keepAliveTimeout >= 0) {
      ftp.setControlKeepAliveTimeout(keepAliveTimeout);
    }
    if (controlKeepAliveReplyTimeout >= 0) {
      ftp.setControlKeepAliveReplyTimeout(controlKeepAliveReplyTimeout);
    }
    ftp.setListHiddenFiles(hidden);

    // suppress login details
    ftp.addProtocolCommandListener(
        new PrintCommandListener(new PrintWriter(System.out), true));

    try {
      int reply;
      if (port > 0) {
        ftp.connect(server, port);
      } else {
        ftp.connect(server);
      }
      System.out.println("Connected to " + server + " on "
                         + (port > 0? port : ftp.getDefaultPort()));

      // After connection attempt, you should check the reply code to verify
      // success.
      reply = ftp.getReplyCode();

      if (!FTPReply.isPositiveCompletion(reply)) {
        ftp.disconnect();
        System.err.println("FTP server refused connection.");
        System.exit(1);
      }
    } catch (IOException e) {
      if (ftp.getDataConnectionMode() ==
          FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
        try {
          ftp.disconnect();
        } catch (IOException f) {
          // do nothing
        }
      }
      System.err.println("Could not connect to server.");
      e.printStackTrace();
      System.exit(1);
    }

    __main:
    try {
      if (account == null) {
        if (!ftp.login(username, password)) {
          ftp.logout();
          error = true;
          break __main;
        }
      } else {
        if (!ftp.login(username, password, account)) {
          ftp.logout();
          error = true;
          break __main;
        }
      }
      System.out.println("Remote system is " + ftp.getSystemType());

      if (binaryTransfer) {
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
      }

      // Use passive mode as default because most of us are
      // behind firewalls these days.
      if (localActive) {
        ftp.enterLocalActiveMode();
      } else {
        ftp.enterLocalPassiveMode();
      }

      ftp.setUseEPSVwithIPv4(useEpsvWithIPv4);

      if (mustCallProtP) {
        ((FTPSClient) ftp).execPBSZ(0);
        ((FTPSClient) ftp).execPROT("P");
      }

      if (storeFile) {
        InputStream input;

        input = new FileInputStream(local);

        ftp.storeFile(remote, input);

        input.close();
      } else if (listFiles) {
        if (lenient) {
          FTPClientConfig config = new FTPClientConfig();
          config.setLenientFutureDates(true);
          ftp.configure(config);
        }

        for (FTPFile f : ftp.listFiles(remote)) {
          System.out.println(f.getRawListing());
          System.out.println(f.toFormattedString());
        }
      } else if (mlsd) {
        for (FTPFile f : ftp.mlistDir(remote)) {
          System.out.println(f.getRawListing());
          System.out.println(f.toFormattedString());
        }
      } else if (mlst) {
        FTPFile f = ftp.mlistFile(remote);
        if (f != null) {
          System.out.println(f.toFormattedString());
        }
      } else if (listNames) {
        for (String s : ftp.listNames(remote)) {
          System.out.println(s);
        }
      } else if (feat) {
        // boolean feature check
        if (remote != null) { // See if the command is present
          if (ftp.hasFeature(remote)) {
            System.out.println("Has feature: " + remote);
          } else {
            if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
              System.out.println("FEAT " + remote + " was not detected");
            } else {
              System.out.println("Command failed: " + ftp.getReplyString());
            }
          }

          // Strings feature check
          String[] features = ftp.featureValues(remote);
          if (features != null) {
            for (String f : features) {
              System.out.println("FEAT " + remote + "=" + f + ".");
            }
          } else {
            if (FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
              System.out.println("FEAT " + remote + " is not present");
            } else {
              System.out.println("Command failed: " + ftp.getReplyString());
            }
          }
        } else {
          if (ftp.features()) {
            // Command listener has already printed the output
          } else {
            System.out.println("Failed: " + ftp.getReplyString());
          }
        }
      } else if (doCommand != null) {
        if (ftp.doCommand(doCommand, remote)) {
          // Command listener has already printed the output
          // for(String s : ftp.getReplyStrings()) {
          // System.out.println(s);
          // }
        } else {
          System.out.println("Failed: " + ftp.getReplyString());
        }
      } else {
        OutputStream output;

        output = new FileOutputStream(local);

        ftp.retrieveFile(remote, output);

        output.close();
      }

      ftp.noop(); // check that control connection is working OK

      ftp.logout();
    } catch (FTPConnectionClosedException e) {
      error = true;
      System.err.println("Server closed connection.");
      e.printStackTrace();
    } catch (IOException e) {
      error = true;
      e.printStackTrace();
    } finally {
      if (ftp.getDataConnectionMode() ==
          FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
        try {
          ftp.disconnect();
        } catch (IOException f) {
          // do nothing
        }
      }
    }

    System.exit(error? 1 : 0);
  } // end main

  private static CopyStreamListener createListener() {
    return new CopyStreamListener() {
      private long megsTotal = 0;

      public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(),
                         event.getBytesTransferred(),
                         event.getStreamSize());
      }

      public void bytesTransferred(long totalBytesTransferred,
                                   int bytesTransferred, long streamSize) {
        long megs = totalBytesTransferred / 1000000;
        for (long l = megsTotal; l < megs; l++) {
          System.err.print("#");
        }
        megsTotal = megs;
      }
    };
  }
}
