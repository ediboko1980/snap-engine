/*
 * Copyright (C) 2014 by Array Systems Computing Inc. http://www.array.ca
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option)
 * any later version.
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, see http://www.gnu.org/licenses/
 */
package org.esa.snap.util;

import org.esa.beam.dataio.dimap.DimapProductConstants;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductReader;
import org.esa.beam.framework.datamodel.Band;
import org.esa.beam.framework.datamodel.MetadataElement;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.datamodel.VirtualBand;
import org.esa.snap.datamodel.AbstractMetadata;
import org.esa.snap.eo.Constants;

import java.io.File;
import java.util.ArrayList;

/**

 */
public class ProductFunctions {

    private final static String[] validExtensions = {".dim", ".n1", ".e1", ".e2", ".h5", ".zip"};
    private final static String[] xmlPrefix = {"product", "tsx1_sar", "tsx2_sar", "tdx1_sar", "tdx2_sar"};

    // valid but not products
    private static final String[] excludedExtensions = { "pix", "tif" };

    private static final String[] nonValidExtensions = {"xsd", "xsl", "xls", "pdf", "doc", "ps", "db", "rtf",
            "ief", "ord", "rrd", "lbl", "aux", "ovr", "brs",
            "self", "report", "raw", "tgz", "pox", "img", "hdr", "ras", "ntf",
            "tfw", "gif", "jpg", "jgw", "log", "html", "htm", "png", "bmp", "kml", "kmz",
            "sav", "7z",  "z", "gz", "tar", "exe", "so", "dll", "bat", "sh",
            "prj", "dbf", "shx", "shp", "ace", "ace2", "tooldes"};
    private static final String[] nonValidprefixes = {"led", "trl", "tra_", "nul", "lea", "dat", "img", "imop", "sarl", "sart", "par_",
            "dfas", "dfdn", "lut",
            "readme", "l1b_iif", "dor_vor", "imagery_", "browse"};

    final static String[] invalidFolders = {"annotation", "measurement", "auxraster", "auxfiles", "imagedata", "preview",
            "support", "quality", "source_images", "schemas"};

    public static boolean isValidProduct(final File file) {
        final String name = file.getName().toLowerCase();
        for (String str : validExtensions) {
            if (name.endsWith(str)) {
                return true;
            }
        }
        if (name.endsWith("xml")) {

            for (String str : xmlPrefix) {
                if (name.startsWith(str)) {
                    return true;
                }
            }
            return false;
        }

        // test with readers
        final ProductReader reader = ProductIO.getProductReaderForInput(file);
        return reader != null;
    }

    // common SAR missions -- non exhaustive
    private static String[] SARMISSIONS = {"SENTINEL1", "ERS", "CSK", "RS", "ALOS", "TSX", "JERS", "UAVSAR"};

    public static boolean isSARProduct(final Product product) {
        final MetadataElement absRoot = AbstractMetadata.getAbstractedMetadata(product);
        if(absRoot != null) {
            final String mission = absRoot.getAttributeString(AbstractMetadata.MISSION);
            if(mission.equals("ENVISAT")) {
                if(product.getProductType().startsWith("ASA")) {
                    return true;
                }
            }
            for(String sar : SARMISSIONS) {
                if (mission.startsWith(sar)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * recursively scan a folder for valid files and put them in pathList
     *
     * @param inputFolder starting folder
     * @param pathList    list of products found
     */
    public static void scanForValidProducts(final File inputFolder, final ArrayList<String> pathList) {
        final ValidProductFileFilter dirFilter = new ValidProductFileFilter();
        final File[] files = inputFolder.listFiles(dirFilter);
        for (File file : files) {
            if (file.isDirectory()) {
                scanForValidProducts(file, pathList);
            } else if (isValidProduct(file)) {
                pathList.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * any files (not folders) that could be products
     */
    public static class ValidProductFileFilter implements java.io.FileFilter {
        private final boolean includeFolders;

        public ValidProductFileFilter() {
            this.includeFolders = false;
        }

        public ValidProductFileFilter(final boolean includeFolders) {
            this.includeFolders = includeFolders;
        }

        public boolean accept(final File file) {
            if (file.isDirectory())
                return includeFolders;

            final String name = file.getName().toLowerCase();
            for (String ext : validExtensions) {
                if (name.endsWith(ext)) {
                    return !name.startsWith("asa_wss");     // exclude wss products
                }
            }
            for (String pre : nonValidprefixes) {
                if (name.startsWith(pre))
                    return false;
            }
            for (String ext : excludedExtensions) {
                if (name.endsWith(ext))
                    return false;
            }
            for (String ext : nonValidExtensions) {
                if (name.endsWith(ext))
                    return false;
            }
            return true;
        }
    }

    public final static DirectoryFileFilter directoryFileFilter = new DirectoryFileFilter();

    /**
     * collect valid folders to scan
     */
    public static class DirectoryFileFilter implements java.io.FileFilter {

        public boolean accept(final File file) {
            if (!file.isDirectory()) return false;
            final String name = file.getName().toLowerCase();
            if (name.endsWith(DimapProductConstants.DIMAP_DATA_DIRECTORY_EXTENSION))
                return false;
            if (name.endsWith("safe"))
                return true;
            for (String foldername : invalidFolders) {
                if (name.endsWith(foldername))
                    return false;
            }
            return true;
        }
    }

    /**
     * Gets a quicker estimate than product.getRawStorageSize, raw storage size in bytes of this product node.
     *
     * @return the size in bytes.
     */
    public static long getRawStorageSize(final Product product) {
        long size = 0;
        if(product != null) {
            for (Band band : product.getBands()) {
                size += band.getRawStorageSize(null);
            }
        }
        return size;
    }

    public static long getTotalPixels(final Product product) {
        long size = 0;
        for (Band band : product.getBands()) {
            if(!(band instanceof VirtualBand)) {
                size += band.getRasterWidth()*band.getRasterHeight();
            }
        }
        return size;
    }

    public static String getProcessingStatistics(final Long totalSeconds) {
        return getProcessingStatistics(totalSeconds, null, null);
    }

    public static String getProcessingStatistics(final Long totalSeconds, final Long totalBytes, final Long totalPixels) {

        String durationStr;
        if (totalSeconds > 120) {
            final float minutes = totalSeconds / 60f;
            durationStr = minutes + " minutes";
        } else {
            durationStr = totalSeconds + " seconds";
        }

        String throughPutStr = "";
        if(totalBytes != null && totalBytes > 0 && totalSeconds > 0) {
            final long BperSec = totalBytes / totalSeconds;
            if(BperSec > Constants.oneBillion) {
                final long GiBperSec = Math.round(totalBytes / (1024.0 * 1024.0 * 1024.0)) / totalSeconds;
                throughPutStr = " (" + GiBperSec + " GB/s)";
            } else if(BperSec > Constants.oneMillion) {
                final long MiBperSec = Math.round(totalBytes / (1024.0 * 1024.0)) / totalSeconds;
                throughPutStr = " (" + MiBperSec + " MB/s";
            } else {
                throughPutStr = " (" + BperSec + " B/s";
            }
        }

        String pixelsRateStr = "";
        if(totalPixels != null && totalPixels > 0 && totalSeconds > 0) {
            final long PperSec = totalPixels / totalSeconds;
            if(PperSec > Constants.oneBillion) {
                final long GiBperSec = Math.round(totalPixels / (1000 * 1000 * 1000)) / totalSeconds;
                pixelsRateStr = " (" + GiBperSec + " GPixel/s)";
            } else if(PperSec > Constants.oneMillion) {
                final long MiBperSec = Math.round(totalPixels / (1000 * 1000)) / totalSeconds;
                pixelsRateStr = " " + MiBperSec + " MPixel/s)";
            } else {
                pixelsRateStr = " " + PperSec + " Pixels/s)";
            }
        }

        return "Processing completed in " + durationStr + throughPutStr + pixelsRateStr;
    }
}
