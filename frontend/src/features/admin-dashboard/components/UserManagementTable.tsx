// src/features/admin-dashboard/components/UserManagementTable.tsx

import React, { useEffect, useState } from 'react';
import { useNotification } from '../../../context/NotificationContext';
import { DataGrid } from '@mui/x-data-grid';
import type { GridColDef, GridRenderCellParams } from '@mui/x-data-grid';
import { adminUserService } from '../services/userService';
import type { UserResponseDTO } from '../../../types/user';
import type { Page } from '../../../types/common';
import { 
  Box, Button, Alert, Dialog, DialogTitle, DialogContent, DialogActions,
  IconButton, Menu, MenuItem, ListItemIcon, ListItemText // <-- New imports
} from '@mui/material';
import DeleteIcon from '@mui/icons-material/Delete';
import EditIcon from '@mui/icons-material/Edit';
import AddIcon from '@mui/icons-material/Add';
import MoreVertIcon from '@mui/icons-material/MoreVert'; // <-- The "three dots" icon
import UserForm from './UserForm';
import type { AdminCreateUserRequestDTO } from '../../../types/admin';


const UserManagementTable: React.FC = () => {
  const { showNotification } = useNotification();
  const [paginationModel, setPaginationModel] = useState({ page: 0, pageSize: 5 });
  const [usersPage, setUsersPage] = useState<Page<UserResponseDTO> | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  // --- State for the modal ---
  const [isModalOpen, setIsModalOpen] = useState(false); // <-- CHECK THIS: Is this state declared?
  const [editingUser, setEditingUser] = useState<UserResponseDTO | null>(null);
  const [formSubmitting, setFormSubmitting] = useState(false);

  // --- NEW: State for the actions dropdown menu ---
  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [selectedUserForMenu, setSelectedUserForMenu] = useState<UserResponseDTO | null>(null);
  const isMenuOpen = Boolean(anchorEl);

  const fetchUsers = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await adminUserService.getAllUsers(paginationModel.page, paginationModel.pageSize);
      setUsersPage(data);
    } catch (err: any) {
      setError(err.message || 'Failed to fetch users.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, [paginationModel]);

  // --- Handlers for Modal ---
  const handleOpenAddModal = () => {
    setEditingUser(null);
    setIsModalOpen(true);
  };

  const handleOpenEditModal = (user: UserResponseDTO) => {
    setEditingUser(user);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingUser(null);
  };
  
  // --- NEW: Handlers for the actions dropdown menu ---
  const handleMenuOpen = (event: React.MouseEvent<HTMLElement>, user: UserResponseDTO) => {
    setAnchorEl(event.currentTarget);
    setSelectedUserForMenu(user);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setSelectedUserForMenu(null);
  };

  // --- Your existing handlers for Edit/Delete/Submit ---
  const handleFormSubmit = async (data: any) => {
      setFormSubmitting(true);
      try {
          if (editingUser) {
              await adminUserService.updateUser(editingUser.userId, data);
              showNotification('User updated successfully!', 'success'); // REPLACED ALERT
          } else {
              await adminUserService.createUser(data);
              showNotification('User created successfully!', 'success'); // REPLACED ALERT
          }
          handleCloseModal();
          fetchUsers();
      } catch (err: any) {
          console.error('Form submission error:', err);
          showNotification(err.message || 'An error occurred.', 'error'); // REPLACED ALERT
      } finally {
          setFormSubmitting(false);
      }
  };
  
  const handleEdit = (user: UserResponseDTO) => {
      handleOpenEditModal(user); 
  };

  const handleDelete = async (id: number) => {
      // We'll replace window.confirm in the next step
      if (window.confirm(`Are you sure you want to delete user with ID: ${id}?`)) {
          try {
              await adminUserService.deleteUser(id);
              showNotification('User deleted successfully!', 'info'); // REPLACED ALERT
              fetchUsers(); 
          } catch (err: any) {
              showNotification(err.message || 'Failed to delete user.', 'error');
          }
      }
  };


  const columns: GridColDef[] = [
    { field: 'userId', headerName: 'ID', width: 90 },
    { field: 'name', headerName: 'Name', flex: 1, minWidth: 150 }, // Use flex for responsiveness
    { field: 'email', headerName: 'Email', flex: 1.5, minWidth: 200 },
    { field: 'mobileNo', headerName: 'Mobile No', width: 150 },
    { field: 'messProvidedUserId', headerName: 'Mess ID', width: 120, sortable: false },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 100,
      align: 'center',
      headerAlign: 'center',
      sortable: false,
      // --- UPDATED: renderCell now just shows the icon button ---
      renderCell: (params: GridRenderCellParams) => {
        const user = params.row as UserResponseDTO;
        return (
          <IconButton
            aria-label="more"
            aria-controls="long-menu"
            aria-haspopup="true"
            onClick={(e) => handleMenuOpen(e, user)}
          >
            <MoreVertIcon />
          </IconButton>
        );
      },
    },
  ];

  if (error && !isModalOpen) {
    return <Alert severity="error">{error}</Alert>;
  }

  return (
    <Box sx={{ height: 450, width: '100%', marginTop: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
        <h3>User Management</h3>
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleOpenAddModal}>
            Add User
        </Button>
      </Box>
      <DataGrid
        rows={usersPage?.content || []}
        columns={columns}
        rowCount={usersPage?.totalElements || 0}
        loading={isLoading}
        pageSizeOptions={[5, 10, 20]}
        paginationModel={paginationModel}
        onPaginationModelChange={setPaginationModel}
        paginationMode="server"
        getRowId={(row) => row.userId}
      />

      {/* --- NEW: The Menu component that acts as the dropdown --- */}
      <Menu
        anchorEl={anchorEl}
        open={isMenuOpen}
        onClose={handleMenuClose}
        MenuListProps={{
          'aria-labelledby': 'long-button',
        }}
      >
        <MenuItem onClick={() => {
          if (selectedUserForMenu) {
            handleEdit(selectedUserForMenu);
          }
          handleMenuClose();
        }}>
          <ListItemIcon>
            <EditIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Edit</ListItemText>
        </MenuItem>

        <MenuItem onClick={() => {
          if (selectedUserForMenu) {
            handleDelete(selectedUserForMenu.userId);
          }
          handleMenuClose();
        }}>
          <ListItemIcon>
            <DeleteIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Delete</ListItemText>
        </MenuItem>
      </Menu>

      {/* User Add/Edit Modal (this part remains the same) */}
      <Dialog open={isModalOpen} onClose={handleCloseModal} fullWidth maxWidth="sm">
    <DialogTitle>{editingUser ? 'Edit User' : 'Add New User'}</DialogTitle>
    <DialogContent>
        {/* The UserForm component goes here */}
        <UserForm 
          onSubmit={handleFormSubmit}
          initialData={editingUser}
          isEditMode={!!editingUser}
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

export default UserManagementTable;