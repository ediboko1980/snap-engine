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

import com.bc.ceres.core.Assert;
import com.bc.ceres.core.ServiceRegistry;
import com.bc.ceres.core.ServiceRegistryManager;
import org.esa.beam.BeamCoreActivator;
import org.esa.beam.framework.dataio.DecodeQualification;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class PlacemarkDescriptorRegistry {

    public final static String PROPERTY_NAME_PLACEMARK_DESCRIPTOR = AbstractPlacemarkDescriptor.PROPERTY_NAME_PLACEMARK_DESCRIPTOR;

    private ServiceRegistry<PlacemarkDescriptor> serviceRegistry;

    public PlacemarkDescriptorRegistry(ServiceRegistry<PlacemarkDescriptor> serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    private PlacemarkDescriptorRegistry() {
        ServiceRegistryManager serviceRegistryManager = ServiceRegistryManager.getInstance();
        serviceRegistry = serviceRegistryManager.getServiceRegistry(PlacemarkDescriptor.class);
        if (!BeamCoreActivator.isStarted()) {
            BeamCoreActivator.loadServices(serviceRegistry);
        }
    }

    public static PlacemarkDescriptorRegistry getInstance() {
        return Holder.instance;
    }

    public static void setInstance(PlacemarkDescriptorRegistry instance) {
        Assert.notNull(instance, "instance");
        Holder.instance = instance;
    }

    public PlacemarkDescriptor getPlacemarkDescriptor(Class<? extends PlacemarkDescriptor> clazz) {
        return getPlacemarkDescriptor(clazz.getName());
    }

    public PlacemarkDescriptor getPlacemarkDescriptor(String className) {
        return serviceRegistry.getService(className);
    }

    public Set<PlacemarkDescriptor> getPlacemarkDescriptors() {
        return serviceRegistry.getServices();
    }

    public List<PlacemarkDescriptor> getValidPlacemarkDescriptors(final SimpleFeatureType featureType) {
        ArrayList<PlacemarkDescriptor> list = new ArrayList<PlacemarkDescriptor>();
        for (PlacemarkDescriptor placemarkDescriptor : getPlacemarkDescriptors()) {
            DecodeQualification qualification = placemarkDescriptor.getCompatibilityFor(featureType);
            if (qualification != DecodeQualification.UNABLE) {
                if (qualification == DecodeQualification.INTENDED) {
                    list.add(placemarkDescriptor);
                } else {
                    list.add(placemarkDescriptor);
                }
            }
        }
        Collections.sort(list, new Comparator<PlacemarkDescriptor>() {
            @Override
            public int compare(PlacemarkDescriptor o1, PlacemarkDescriptor o2) {
                boolean isO1Intended = o1.getCompatibilityFor(featureType) == DecodeQualification.INTENDED;
                boolean isO2Intended = o2.getCompatibilityFor(featureType) == DecodeQualification.INTENDED;
                if (isO1Intended && !isO2Intended) {
                    return -1;
                } else if (!isO1Intended && isO2Intended) {
                    return 1;
                } else if (isO1Intended && isO2Intended) {
                    boolean hasO1PlacemarkDescriptor = o1.getBaseFeatureType().getUserData().containsKey(PROPERTY_NAME_PLACEMARK_DESCRIPTOR);
                    boolean hasO2PlacemarkDescriptor = o2.getBaseFeatureType().getUserData().containsKey(PROPERTY_NAME_PLACEMARK_DESCRIPTOR);
                    if (hasO1PlacemarkDescriptor && !hasO2PlacemarkDescriptor) {
                        return -1;
                    } else if (!hasO1PlacemarkDescriptor && hasO2PlacemarkDescriptor) {
                        return 1;
                    }
                }
                return 0;
            }
        });
        return list;
    }

    public PlacemarkDescriptor getBestPlacemarkDescriptor(SimpleFeatureType featureType) {
        PlacemarkDescriptor suitablePlacemarkDescriptor = null;
        for (PlacemarkDescriptor placemarkDescriptor : getPlacemarkDescriptors()) {
            DecodeQualification qualification = placemarkDescriptor.getCompatibilityFor(featureType);
            if (qualification == DecodeQualification.INTENDED) {
                return placemarkDescriptor;
            } else if (qualification == DecodeQualification.SUITABLE) {
                suitablePlacemarkDescriptor = placemarkDescriptor;
            }
        }
        return suitablePlacemarkDescriptor;
    }

    private static class Holder {

        private static PlacemarkDescriptorRegistry instance = new PlacemarkDescriptorRegistry();
    }

}
