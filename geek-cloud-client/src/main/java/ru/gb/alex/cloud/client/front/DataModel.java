package ru.gb.alex.cloud.client.front;

import javax.swing.table.AbstractTableModel;

public class DataModel extends AbstractTableModel {
    private final String[] columnNames;
    private String[][] data;

    DataModel(String[][] data, String[] columnNames) {
        this.data = data;
        this.columnNames = columnNames;
    }

    public void setData(String[][] data) {
        this.data = data;
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

    public String getValueAt(int row, int column) {
        return data[row][column];
    }
}
