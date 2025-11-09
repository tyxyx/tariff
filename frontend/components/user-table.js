"use client";

import { useEffect, useState, useCallback } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ConfirmDialog } from "@/components/confirm-dialog"; // Reusable Dialog
import { colors } from "@/styles/colors";
import { apiFetch } from "@/utils/apiClient";

export function UserTable() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingEmail, setEditingEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [actionError, setActionError] = useState(null);
  const [fetchError, setFetchError] = useState(null);

  // 1. NEW STATE: To track the intended action (delete or role change) and the new role
  const [dialogAction, setDialogAction] = useState({
    type: "delete",
    newRole: null,
  });

  // Function to fetch users (used for initial load and refetch)
  const fetchUsers = useCallback(async () => {
    setFetchError(null);
    try {
      setLoading(true);
      const res = await apiFetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users/`
      );
      if (!res.ok) {
        throw new Error(`Failed to fetch users. Status: ${res.status}`);
      }
      const data = await res.json();
      setUsers(data);
    } catch (err) {
      console.error("Error fetching users:", err);
      setFetchError(err.message || "Network error during initial fetch.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  // --- API Functions ---

  const handleDelete = async (userEmail) => {
    setActionError(null);
    try {
      const res = await apiFetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users/`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email: userEmail }),
        }
      );
      if (res.ok) {
        fetchUsers();
      } else {
        setActionError(`Failed to delete user. Status: ${res.status}`);
      }
    } catch (err) {
      console.error("Error deleting user:", err);
      setActionError("Network error during delete operation.");
    }
  };

  // Handle role update API call
  const handleRoleUpdate = async (userEmail, newRole) => {
    setActionError(null);
    try {
      const res = await apiFetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users/upgrade-role`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            email: userEmail,
          }),
        }
      );
      if (res.ok) {
        alert(`Role updated to ${newRole} successfully!`);
        fetchUsers(); // Refresh data to show new role
      } else {
        setActionError(`Failed to update role. Status: ${res.status}`);
      }
    } catch (err) {
      console.error("Error updating role:", err);
      setActionError("Network error during role update.");
    }
  };

  // --- Dialog Control Functions ---

  const openDeleteDialog = (user) => {
    setSelectedUser(user);
    setDialogAction({ type: "delete", newRole: null });
    setIsDialogOpen(true);
  };

  const openRoleUpdateDialog = (user, role) => {
    setSelectedUser(user);
    setDialogAction({ type: "role", newRole: role });
    setIsDialogOpen(true);
  };

  const handleConfirmAction = () => {
    if (!selectedUser) return;

    if (dialogAction.type === "delete") {
      handleDelete(selectedUser.username);
    } else if (dialogAction.type === "role" && dialogAction.newRole) {
      handleRoleUpdate(selectedUser.username, dialogAction.newRole);
    }
    setIsDialogOpen(false);
  };

  // --- Render Logic ---

  const currentError = fetchError || actionError;

  if (currentError) {
    return (
      <div className="p-4 rounded-md text-red-700 bg-red-100 border border-red-400">
        <h2 className="font-bold text-lg">⚠️ Error</h2>
        <p>**{currentError}**</p>
        <Button
          onClick={fetchUsers}
          className="mt-3 bg-red-600 hover:bg-red-700 text-white"
        >
          Try Again
        </Button>
      </div>
    );
  }

  if (loading) return <p>Loading users...</p>;

  return (
    <div className="overflow-x-auto">
      <table className="min-w-full border-collapse border border-gray-300 rounded-md">
        <thead>
          <tr style={{ backgroundColor: colors.card }}>
            <th className="p-3 text-left border-b">Email</th>
            <th className="p-3 text-left border-b">Role</th>
            <th className="p-3 text-left border-b">Actions</th>
          </tr>
        </thead>
        <tbody>
          {users.map((user) => (
            <tr key={user.username} className="border-b">
              <td className="p-3">{user.username}</td>
              <td className="p-3 capitalize">{user.role}</td>
              <td className="p-3">
                <div className="flex flex-col md:flex-row gap-3 items-center">
                  {/* Role Change Buttons */}
                  {user.role === "admin" ? (
                    <Button
                      onClick={() => openRoleUpdateDialog(user, "user")}
                      className="bg-yellow-600 hover:bg-yellow-700 text-white"
                    >
                      Downgrade to User
                    </Button>
                  ) : (
                    <Button
                      onClick={() => openRoleUpdateDialog(user, "admin")}
                      className="bg-green-600 hover:bg-green-700 text-white"
                    >
                      Upgrade to Admin
                    </Button>
                  )}

                  <Button
                    onClick={() => openDeleteDialog(user)}
                    className="bg-red-600 hover:bg-red-700 text-white"
                  >
                    Delete
                  </Button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 3. Conditional ConfirmDialog Rendering */}
      {isDialogOpen && selectedUser && (
        <ConfirmDialog
          title={
            dialogAction.type === "delete"
              ? "Confirm Deletion"
              : "Confirm Role Change"
          }
          message={
            dialogAction.type === "delete"
              ? `Are you sure you want to delete ${selectedUser.username}? This action cannot be undone.`
              : `Are you sure you want to change ${selectedUser.username}'s role to **${dialogAction.newRole}**?`
          }
          onConfirm={handleConfirmAction}
          onCancel={() => setIsDialogOpen(false)}
          // Pass dynamic button props to ConfirmDialog
          confirmText={
            dialogAction.type === "delete" ? "Delete" : "Confirm Change"
          }
          confirmColor={
            dialogAction.type === "delete" ? "bg-red-600" : "bg-green-600"
          }
        />
      )}
    </div>
  );
}
