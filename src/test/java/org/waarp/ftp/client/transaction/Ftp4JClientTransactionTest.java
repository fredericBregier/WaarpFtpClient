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

package org.waarp.ftp.client.transaction;

import org.waarp.ftp.client.WaarpFtp4jClient;

/**
 * FTP Client using FTP4J with the test scenario
 *
 * @author frederic
 */
public class Ftp4JClientTransactionTest extends WaarpFtp4jClient {
  /**
   * @param server
   * @param port
   * @param username
   * @param passwd
   * @param account
   */
  public Ftp4JClientTransactionTest(String server, int port, String username,
                                    String passwd, String account, int isSsl) {
    super(server, port, username, passwd, account, false, isSsl, 0, 10000);
  }

  /**
   * Ask to transfer a file
   *
   * @param local
   * @param remote
   * @param store
   *
   * @return True if the file is correctly transfered
   */
  public boolean transferFile(String local, String remote, boolean store) {
    boolean status = super.transferFile(local, remote, store? 1 : 0);
    /*
     * if (status) { String [] results = this.executeSiteCommand("XCRC "+remote); for (String
     * string : results) { System.err.println("XCRC: "+string); } results =
     * this.executeSiteCommand("XMD5 "+remote); for (String string : results) {
     * System.err.println("XMD5: "+string); } results =
     * this.executeSiteCommand("XSHA1 "+remote); for (String string : results) {
     * System.err.println("XSHA1: "+string); } }
     */
    return status;
  }
}
