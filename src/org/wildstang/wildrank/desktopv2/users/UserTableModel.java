package org.wildstang.wildrank.desktopv2.users;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class UserTableModel extends AbstractTableModel {

	public static final int COLUMN_NAME_INDEX = 0;
	public static final int COLUMN_ID_INDEX = 1;
	public static final int COLUMN_ADMIN_INDEX = 2;

	private static final String[] COLUMN_NAMES = { "Name", "ID", "Admin" };

	private List<User> users;

	public UserTableModel(List<User> users) {
		this.users = users;
	}

	@Override
	public int getRowCount() {
		return users.size();
	}

	@Override
	public int getColumnCount() {
		return COLUMN_NAMES.length;
	}
	
	@Override
	public String getColumnName(int column) {
		return COLUMN_NAMES[column];
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COLUMN_NAME_INDEX:
			return String.class;
		case COLUMN_ID_INDEX:
			return String.class;
		case COLUMN_ADMIN_INDEX:
			return Boolean.class;
		default:
			return null;
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		User user = users.get(rowIndex);

		switch (columnIndex) {
		case COLUMN_NAME_INDEX:
			return user.getName();
		case COLUMN_ID_INDEX:
			return user.getId();
		case COLUMN_ADMIN_INDEX:
			return user.isAdmin();
		default:
			return null;
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		if (rowIndex < 0 || rowIndex >= users.size()) {
			return;
		}
		
		User user = users.get(rowIndex);
		switch (columnIndex) {
		case COLUMN_NAME_INDEX:
			user.setName((String) value);
			break;
		case COLUMN_ID_INDEX:
			user.setId((String) value);
			break;
		case COLUMN_ADMIN_INDEX:
			user.setAdmin((Boolean) value);
			break;
		default:
			return;
		}
	}

}
