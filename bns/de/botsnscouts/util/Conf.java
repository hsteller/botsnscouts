/*
 *******************************************************************
 *        Bots 'n' Scouts - Multi-Player networked Java game       *
 *                                                                 *
 * Copyright (C) 2001 scouties.                                    *
 * Contact botsnscouts-devel@sf.net                                *
 *******************************************************************

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, in version 2 of the License.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program, in a file called COPYING in the top
 directory of the Bots 'n' Scouts distribution; if not, write to
 the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 Boston, MA  02111-1307  USA

 *******************************************************************/

package de.botsnscouts.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Category;

/** Used to access configuration that users may change */
public class Conf {
    public static final Category CAT = Category.getInstance(de.botsnscouts.BotsNScouts.class);

    private static final String CONFNAME = "bns.config";

    private static final String bnsHome;

    private static Properties properties;

    private static final char MULTIPLE_PROP_SEPARATOR = ',';

    // private static final String PROPNAME_METASERVER_ENABLED = "enableMetaserver";
    public static final boolean IS_METASERVER_ENABLED = false;// Conf.getBooleanProperty(
    // Conf.PROPNAME_METASERVER_ENABLED, false);

    static {
        // Set bnsHome: if it's explicitly set, we take that
        String s = System.getProperty("bns.home");
        if (s == null) // probably windows, use current working dir
            s = System.getProperty("user.dir");
        bnsHome = s;
        CAT.debug("bnsHome: " + bnsHome);

        // Set the properties: a) the default one
        properties = new Properties();
        try {
            InputStream in = de.botsnscouts.BotsNScouts.class.getResourceAsStream("conf/" + CONFNAME);
            if (in == null) {
                CAT.fatal("default bns.config not found.");
                throw new RuntimeException("default bns.config not found");
            }
            properties.load(in);
            CAT.debug("defautl bns.config loaded.");
        }
        catch (IOException e) {
            CAT.fatal("default bns.config not found.");
            throw new RuntimeException("default bns.config not found");
        }
        // b) a user-defined one overrides that
        try {
            Properties p = new Properties(properties);
            p.load(new FileInputStream(bnsHome + System.getProperty("file.separator") + CONFNAME));
            properties = p;
            CAT.info("user-defined bns.config loaded.");
        }
        catch (IOException e) {
            CAT.info("no user-defined bns.config found.");
        }
        // c) System-wide properties override that -- see getProperty
    }

    /**
     * Used to access the bns installation directory.
     * 
     * @return a <code>String</code> value representing the absolute path there.
     */
    public static String getBnsHome() {
        return bnsHome;
    }

    public static String getDefaultRobName() {
        String n = Conf.getProperty("robot.name"); // Set in bns.config?
        if (n == null || n.equals(""))
            n = Conf.getProperty("user.name"); // System property
        if (n == null || n.equals(""))
            n = KrimsKrams.randomName(); // KrimsKrams-Random
        return n.substring(0, 1).toUpperCase() + n.substring(1, n.length());
    }

    public static String getProperty(String key) {
        String data = System.getProperty(key);
        if (data == null)
            data = properties.getProperty(key);
        if (data != null)
            return Encoder.propertyDecode(data);
        return data;
    }

    public static String[] getMultipleProperty(String key) {
        String data = System.getProperty(key);
        if (data == null)
            data = properties.getProperty(key);
        String[] back = null;
        if (data != null) {
            if (data.indexOf(MULTIPLE_PROP_SEPARATOR) > -1) {
                StringTokenizer st = new StringTokenizer(data, "" + MULTIPLE_PROP_SEPARATOR);
                back = new String[st.countTokens()];
                for (int i = 0; i < back.length; i++) {
                    back[i] = Encoder.propertyDecode(st.nextToken());
                }
            }
            else {
                back = new String[] { Encoder.propertyDecode(data) };
            }
        }
        return back;
    }

    public static Vector<String> getMultiplePropertyVector(String key) {
        String data = System.getProperty(key);
        if (data == null)
            data = properties.getProperty(key);
        Vector<String> back = new Vector<String>();
        if (data != null) {
            if (data.indexOf(MULTIPLE_PROP_SEPARATOR) > -1) {
                StringTokenizer st = new StringTokenizer(data, "" + MULTIPLE_PROP_SEPARATOR);
                int length = st.countTokens();
                for (int i = 0; i < length; i++) {
                    back.addElement(Encoder.propertyDecode(st.nextToken()));
                }
            }
            else {
                back.add(Encoder.propertyDecode(data));
            }
        }
        return back;
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String data = getProperty(key);
        boolean ret = defaultValue;

        if (data != null) {
            String d = data.trim();
            if (d.equalsIgnoreCase("true")) {
                ret = true;
            }
            else
                if (d.equalsIgnoreCase("false")) {
                    ret = true;
                }
        }

        return ret;
    }

    public static int getIntProperty(String key, int defaultValue) {
        String data = getProperty(key);
        int ret = defaultValue;
        try {
            ret = Integer.parseInt(data);
        }
        catch (Exception e) {
            CAT.warn(e.getMessage());
        }
        return ret;
    }

    public static void setProperty(String key, String data) {
        properties.setProperty(key, Encoder.propertyEncode(data));
    }

    public static void setMultipleProperty(String key, String[] values) {
        if (values == null || values.length < 1) {
            properties.setProperty(key, "");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(Encoder.propertyEncode(values[0]));
        for (int i = 1; i < values.length; i++) {
            sb.append(MULTIPLE_PROP_SEPARATOR).append(values[i]);
        }
        properties.setProperty(key, sb.toString());
    }

    public static void setMultipleProperty(String key, Vector<String> values) {
        if (values == null || values.size() < 1) {
            properties.setProperty(key, "");
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(Encoder.propertyEncode(values.elementAt(0).toString()));
        for (int i = 1; i < values.size(); i++) {
            sb.append(MULTIPLE_PROP_SEPARATOR).append(values.elementAt(i));
        }
        properties.setProperty(key, sb.toString());
    }

    public static void saveProperties() {
        try {
            File file = new File(bnsHome + System.getProperty("file.separator") + CONFNAME);
            OutputStream ostream = new FileOutputStream(file);
            properties.store(ostream, null);
        }
        catch (IOException e) {
            CAT.info("Save of user-defined bns.config failed.");
        }
    }

    public static String getDefaultMetaServer() {
        String meta = Conf.getProperty("meta.server");
        if (meta == null || meta.equals(""))
            meta = "www.botsnscouts.de";
        return meta;
    }

    public static int getDefaultMetaServerPort() {
        String portString = Conf.getProperty("meta.port");
        if (portString != null && !portString.equals(""))
            return Integer.parseInt(portString);
        else
            return 8725;
    }

}
