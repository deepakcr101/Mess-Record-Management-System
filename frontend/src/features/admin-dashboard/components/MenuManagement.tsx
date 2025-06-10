// src/features/admin-dashboard/components/MenuManagement.tsx
import React, { useEffect, useState } from 'react';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { menuService } from '../../menu/services/menuService';
import type { MenuItemDTO } from '../../../types/menu';
import type { Page } from '../../../types/common';
import { Box, Button, Dialog, DialogTitle, DialogContent, DialogActions, Chip } from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import MenuItemForm from '../../menu/components/MenuItemForm'; // Import the form

const MenuManagement: React.FC = () => {
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 5 });
  const [menuItemsPage, setMenuItemsPage] = useState<Page<MenuItemDTO> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<MenuItemDTO | null>(null);
  const [formSubmitting, setFormSubmitting] = useState(false);

  const fetchMenuItems = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await menuService.getAllMenuItems(paginationModel.page, paginationModel.pageSize);
      setMenuItemsPage(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch menu items.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchMenuItems();
  }, [paginationModel]);

  const handleOpenAddModal = () => {
    setEditingItem(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (item: MenuItemDTO) => {
    setEditingItem(item);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingItem(null);
  };

  const handleFormSubmit = async (data: MenuItemDTO) => {
      setFormSubmitting(true);
      try {
          if (editingItem) {
              await menuService.updateMenuItem(editingItem.itemId, data);
              alert('Menu item updated successfully!');
          } else {
              await menuService.createMenuItem(data);
              alert('Menu item created successfully!');
          }
          handleCloseModal();
          fetchMenuItems(); // Refresh table
      } catch (err: any) {
          console.error('Menu form submission error:', err);
          alert(`Error: ${err.message || 'An error occurred.'}`);
      } finally {
          setFormSubmitting(false);
      }
  };

  const handleDelete = async (id: number) => {
      if (window.confirm(`Are you sure you want to delete menu item with ID: ${id}?`)) {
          try {
              await menuService.deleteMenuItem(id);
              alert('Menu item deleted successfully!');
              fetchMenuItems(); 
          } catch (err: any) {
              setError(err.message || 'Failed to delete menu item.');
          }
      }
  };

  const columns: GridColDef[] = [
    { field: 'itemId', headerName: 'ID', width: 90 },
    { field: 'name', headerName: 'Name', width: 250 },
    { field: 'category', headerName: 'Category', width: 130 },
    { field: 'price', headerName: 'Price (₹)', width: 100, type: 'number' },
    { field: 'isAvailable', headerName: 'Available', width: 120, type: 'boolean',
      renderCell: (params) => (
        <Chip label={params.value ? "Yes" : "No"} color={params.value ? "success" : "error"} size="small" />
      )
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 200,
      sortable: false,
      renderCell: (params: GridRenderCellParams) => (
        <>
          <Button onClick={() => handleOpenEditModal(params.row)} startIcon={<EditIcon />} size="small" sx={{ mr: 1 }}>Edit</Button>
          <Button onClick={() => handleDelete(params.row.itemId)} startIcon={<DeleteIcon />} color="error" size="small">Delete</Button>
        </>
      ),
    },
  ];

  return (
    <Box sx={{ width: '100%', marginTop: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <h3>Menu Item Management</h3>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenAddModal}>
            Add Menu Item
        </Button>
      </Box>
      <DataGrid
        autoHeight
        rows={menuItemsPage?.content || []}
        columns={columns}
        rowCount={menuItemsPage?.totalElements || 0}
        loading={isLoading}
        pageSizeOptions={[5, 10, 20]}
        paginationModel={paginationModel}
        onPaginationModelChange={setPaginationModel}
        paginationMode="server"
        getRowId={(row) => row.itemId}
      />

      <Dialog open={isModalOpen} onClose={handleCloseModal} fullWidth maxWidth="sm">
          <DialogTitle>{editingItem ? 'Edit Menu Item' : 'Add New Menu Item'}</DialogTitle>
          <DialogContent>
              <MenuItemForm 
                onSubmit={handleFormSubmit}
                initialData={editingItem}
                isEditMode={!!editingItem}
                isLoading={formSubmitting}
              />
          </DialogContent>
          <DialogActions>
              <Button onClick={handleCloseModal}>Cancel</Button>
          </DialogActions>
      </Dialog>
    </Box>
  );
};

export default MenuManagement;