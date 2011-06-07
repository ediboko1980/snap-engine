/*
 * Copyright (C) 2010 Brockmann Consult GmbH (info@brockmann-consult.de)
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

package org.esa.beam.framework.datamodel;

import org.opengis.feature.simple.SimpleFeatureType;

import java.awt.*;

public class PinDescriptor extends AbstractPlacemarkDescriptor {

    public static PinDescriptor getInstance() {
        return (PinDescriptor) PlacemarkDescriptorRegistry.getInstance().getPlacemarkDescriptor(PinDescriptor.class.getName());
    }

    @Override
    public boolean isCompatibleWith(SimpleFeatureType featureType) {
        return featureType.getTypeName().equals("org.esa.beam.Pin");
    }

    @Override
    public String getShowLayerCommandId() {
        return "showPinOverlay";
    }

    @Override
    public String getRoleName() {
        return "pin";
    }

    @Override
    public String getRoleLabel() {
        return "pin";
    }

    @Override
    public Image getCursorImage() {
        return null;
    }

    @Override
    public Point getCursorHotSpot() {
        return new Point();
    }


    @Override
    public PlacemarkGroup getPlacemarkGroup(Product product) {
        return product.getPinGroup();
    }

    @Override
    public PlacemarkSymbol createDefaultSymbol() {
        return PlacemarkSymbol.createDefaultPinSymbol();
    }

    @Override
    public PixelPos updatePixelPos(GeoCoding geoCoding, GeoPos geoPos, PixelPos pixelPos) {
        if (geoCoding == null || !geoCoding.canGetPixelPos() || geoPos == null) {
            return pixelPos;
        }
        return geoCoding.getPixelPos(geoPos, pixelPos);
    }

    @Override
    public GeoPos updateGeoPos(GeoCoding geoCoding, PixelPos pixelPos, GeoPos geoPos) {
        if (geoCoding == null || !geoCoding.canGetGeoPos()) {
            return geoPos;
        }
        return geoCoding.getGeoPos(pixelPos, geoPos);

    }
}
