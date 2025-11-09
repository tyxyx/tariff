"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ConfirmDialog } from "@/components/confirm-dialog";
import { colors } from "@/styles/colors";
import { apiFetch } from "@/utils/apiClient"; // import apiFetch

export function UserTable() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingEmail, setEditingEmail] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        setLoading(true);
        const res = await apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/`);
        const data = await res.json();
        setUsers(data);
      } catch (err) {
        console.error("Error fetching users:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

  const handleDelete = async (username) => {
    try {
      const res = await apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/${username}`, {
        method: "DELETE",
      });
      if (res.ok) {
        setUsers((prev) => prev.filter((u) => u.username !== username));
      }
    } catch (err) {
      console.error("Error deleting user:", err);
    }
  };

  const handleEmailUpdate = async (username) => {
    try {
      const res = await apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/${username}/username`, {
        method: "PUT",
        body: JSON.stringify({ username: editingEmail }),
      });
      if (res.ok) {
        alert("Email updated successfully!");
        setEditingEmail("");
        // Optionally refresh users
      }
    } catch (err) {
      console.error("Error updating email:", err);
    }
  };

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
                  <Button
                    onClick={() => {
                      setSelectedUser(user);
                      setIsDialogOpen(true);
                    }}
                    className="bg-red-600 hover:bg-red-700 text-white"
                  >
                    Delete
                  </Button>

                  <div className="flex items-center gap-2">
                    <Input
                      label="New Email"
                      type="email"
                      value={editingEmail}
                      onChange={(e) => setEditingEmail(e.target.value)}
                    />
                    <Button
                      onClick={() => handleEmailUpdate(user.username)}
                      className="bg-blue-600 hover:bg-blue-700 text-white"
                    >
                      Update
                    </Button>
                  </div>
                </div>
              </td>
            </tr>
          ))}
        </tbody>

      </table>

      {isDialogOpen && selectedUser && (
        <ConfirmDialog
          title="Confirm Deletion"
          message={`Are you sure you want to delete ${selectedUser.username}?`}
          onConfirm={() => {
            handleDelete(selectedUser.username);
            setIsDialogOpen(false);
          }}
          onCancel={() => setIsDialogOpen(false)}
        />
      )}
    </div>
  );
}
