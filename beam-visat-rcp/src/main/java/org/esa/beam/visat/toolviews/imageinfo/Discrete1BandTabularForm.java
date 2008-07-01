package org.esa.beam.visat.toolviews.imageinfo;

import com.jidesoft.grid.ColorCellEditor;
import com.jidesoft.grid.ColorCellRenderer;
import org.esa.beam.framework.datamodel.ColorPaletteDef;
import org.esa.beam.framework.datamodel.ImageInfo;
import org.esa.beam.framework.ui.product.ProductSceneView;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.Color;
import java.awt.Component;
import java.text.NumberFormat;

class Discrete1BandTabularForm implements ColorManipulationChildForm {
    private final ColorManipulationForm parentForm;
    private JComponent contentPanel;
    private ImageInfoTableModel tableModel;
    private final TableModelListener applyEnablerTML;

    public Discrete1BandTabularForm(ColorManipulationForm parentForm) {
        this.parentForm = parentForm;
        tableModel = new ImageInfoTableModel(null);
        applyEnablerTML = parentForm.createApplyEnablerTableModelListener();

        final JTable table = new JTable(tableModel);
        final ColorCellRenderer colorCellRenderer = new ColorCellRenderer();
        colorCellRenderer.setColorValueVisible(false);
        table.setDefaultRenderer(Color.class, colorCellRenderer);
        table.setDefaultEditor(Color.class, new ColorCellEditor());
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(1).setPreferredWidth(140);

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        final JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.getViewport().setPreferredSize(table.getPreferredSize());
        contentPanel = tableScrollPane;
    }

    public void handleFormShown(ProductSceneView productSceneView) {
        tableModel.setImageInfo(parentForm.getImageInfo());
        tableModel.addTableModelListener(applyEnablerTML);
    }

    public void handleFormHidden(ProductSceneView productSceneView) {
        tableModel.removeTableModelListener(applyEnablerTML);
        tableModel.setImageInfo(null);
    }

    public void updateFormModel(ProductSceneView productSceneView) {
        tableModel.setImageInfo(parentForm.getImageInfo());
    }

    public AbstractButton[] getButtons() {
        return new AbstractButton[]{}; // todo
    }

    public Component getContentPanel() {
        return contentPanel;
    }

    private static class PercentageRenderer extends DefaultTableCellRenderer {

        private final NumberFormat formatter;

        public PercentageRenderer() {
            setHorizontalAlignment(JLabel.RIGHT);
            formatter = NumberFormat.getPercentInstance();
            formatter.setMinimumFractionDigits(1);
            formatter.setMaximumFractionDigits(3);
        }

        @Override
        public void setValue(Object value) {
            setText((value == null) ? "" : formatter.format(value));
        }
    }

    private static class ImageInfoTableModel extends AbstractTableModel {

        private ImageInfo imageInfo;
        private static final String[] COLUMN_NAMES = new String[]{"Label", "Color", "Value", "Freq."};
        private static final Class<?>[] COLUMN_TYPES = new Class<?>[]{String.class, Color.class, String.class, Double.class};

        private ImageInfoTableModel(ImageInfo imageInfo) {
            this.imageInfo = imageInfo;
        }

        public ImageInfo getImageInfo() {
            return imageInfo;
        }

        public void setImageInfo(ImageInfo imageInfo) {
            this.imageInfo = imageInfo;
            fireTableDataChanged();
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMN_NAMES[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return COLUMN_TYPES[columnIndex];
        }

        public int getColumnCount() {
            return COLUMN_NAMES.length;
        }

        public int getRowCount() {
            return imageInfo != null ? imageInfo.getColorPaletteDef().getNumPoints() : 0;
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            final ColorPaletteDef.Point point = imageInfo.getColorPaletteDef().getPointAt(rowIndex);
            if (columnIndex == 0) {
                return point.getLabel();
            } else if (columnIndex == 1) {
                final Color color = point.getColor();
                return color.equals(ImageInfo.NO_COLOR) ? null : color;
            } else if (columnIndex == 2) {
                return Double.isNaN(point.getSample()) ? "Uncoded" : "" + (int) point.getSample();
            } else if (columnIndex == 3) {
                // todo - return abundance percentage
                if (imageInfo == null
                        || imageInfo.getHistogramBins() == null
                        || imageInfo.getHistogramBins().length >= getRowCount()) {
                    return Double.NaN;
                }
                return imageInfo.getHistogramBins()[rowIndex];
            } else if (columnIndex == 4) {
                return 0.0;
            }
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            final ColorPaletteDef.Point point = imageInfo.getColorPaletteDef().getPointAt(rowIndex);
            if (columnIndex == 0) {
                point.setLabel((String) aValue);
                fireTableCellUpdated(rowIndex, columnIndex);
            } else if (columnIndex == 1) {
                final Color color = (Color) aValue;
                point.setColor(color == null ? ImageInfo.NO_COLOR : color);
                fireTableCellUpdated(rowIndex, columnIndex);
            }
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 || columnIndex == 1;
        }

    }
}