package ru.gb.alex.cloud.client.inter;

import javax.swing.table.AbstractTableModel;

public class DataModel extends AbstractTableModel {
    private String[] columnNames = null;
    private Object[][] data = null;

    DataModel(Object[][] data, String[] columnNames) {
        this.data = data;
        this.columnNames = columnNames;
    }

    public int getColumnCount(){
        return columnNames.length;
    }

    public String getColumnName(int column) {
        return columnNames[column];
    }

    public int getRowCount() {
        return data.length;
    }

    public Class<?> getColumnClass(int column) {
        return data[0][column].getClass();
    }

    public Object getValueAt(int row, int column) {
        return data[row][column];
    }
}
