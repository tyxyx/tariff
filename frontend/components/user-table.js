"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
<<<<<<< HEAD
import { Input } from "@/components/ui/input";
import { ConfirmDialog } from "@/components/confirm-dialog";
=======
import { ConfirmDialog } from "@/components/confirm-dialog"; // Reusable Dialog
>>>>>>> e55f64a (added blockers and allow admin to add user)
import { colors } from "@/styles/colors";
import { apiFetch } from "@/utils/apiClient"; // import apiFetch

export function UserTable() {
  const [users, setUsers] = useState([]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingEmail, setEditingEmail] = useState("");
  const [loading, setLoading] = useState(false);
<<<<<<< HEAD
=======
  const [actionError, setActionError] = useState(null);
  const [fetchError, setFetchError] = useState(null);
  const [currentUserRole, setCurrentUserRole] = useState(null);

  //  To track the intended action (delete or role change) and the new role
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
>>>>>>> e55f64a (added blockers and allow admin to add user)

  useEffect(() => {
    const fetchUsers = async () => {
      try {
<<<<<<< HEAD
        setLoading(true);
        const res = await apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/`);
        const data = await res.json();
        setUsers(data);
=======
        // Fetch current user and all users at the same time
        const [meRes, usersRes] = await Promise.all([
          apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/me`),
          apiFetch(`${process.env.NEXT_PUBLIC_API_URL}/api/users/`),
        ]);

        if (!meRes.ok) {
          throw new Error(
            `Failed to fetch current user. Status: ${meRes.status}`
          );
        }
        const meData = await meRes.json();
        setCurrentUserEmail(meData.username);
        setCurrentUserRole(meData.role);

        if (!usersRes.ok) {
          throw new Error(
            `Failed to fetch user list. Status: ${usersRes.status}`
          );
        }
        const usersData = await usersRes.json();
        setUsers(usersData);
>>>>>>> e55f64a (added blockers and allow admin to add user)
      } catch (err) {
        console.error("Error fetching users:", err);
      } finally {
        setLoading(false);
      }
    };
    fetchUsers();
  }, []);

<<<<<<< HEAD
  const handleDelete = async (username) => {
=======
    fetchInitialData();
  }, []);

  // --- API Functions ---

  const handleDelete = async (userEmail) => {
    setActionError(null);
>>>>>>> e55f64a (added blockers and allow admin to add user)
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
<<<<<<< HEAD
        alert("Email updated successfully!");
        setEditingEmail("");
        // Optionally refresh users
=======
        alert(`Role updated to ${newRole} successfully!`);
        fetchUsers();
      } else {
        setActionError(`Failed to update role. Status: ${res.status}`);
>>>>>>> e55f64a (added blockers and allow admin to add user)
      }
    } catch (err) {
      console.error("Error updating email:", err);
    }
  };

<<<<<<< HEAD
=======
  const handleRoleDowngrade = async (userEmail, newRole) => {
    setActionError(null);
    try {
      const res = await apiFetch(
        `${process.env.NEXT_PUBLIC_API_URL}/api/users/downgrade-role`, // <-- USE THE CORRECT ENDPOINT
        {
          method: "PUT", // Based on your image
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
        fetchUsers(); // Refresh data
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
    }
    if (dialogAction.newRole === "admin") {
      handleRoleUpdate(selectedUser.username, dialogAction.newRole);
    } else if (dialogAction.newRole === "user") {
      handleRoleDowngrade(selectedUser.username, dialogAction.newRole);
    }
    setIsDialogOpen(false);
  };

  // --- Render Logic ---

  const currentError = fetchError || actionError;
  let statusCode;
  let errorMessage = "An unexpected error occurred.";

  if (currentError) {
    if (typeof currentError === "object") {
      statusCode = currentError.status;
      errorMessage = currentError.message || errorMessage;
    } else if (typeof currentError === "string") {
      if (currentError === "Failed to fetch") {
        statusCode = "FETCH";
        errorMessage =
          "Cannot connect to the server. Please check your network or try again later.";
      } else {
        const match = currentError.match(/(\d{3})/);
        statusCode = match ? match[1] : null;
        errorMessage = currentError;
      }
    }
  }

  if (currentError) {
    // Fail page block variant for fetch/network/403 errors
    const is403 = statusCode === "403" || errorMessage.includes("403");
    const isFetch =
      statusCode === "FETCH" || errorMessage === "Failed to fetch";
    return (
      <div
        className="min-h-screen flex flex-col items-center justify-center"
        style={{
          background: "#171924",
          color: "#ff5151",
        }}
      >
        <div
          className="p-8 rounded-lg border shadow-lg"
          style={{
            background: "#1b1e2b",
            borderColor: "#512f3d",
            color: "#ff5151",
            boxShadow: "0 2px 10px rgba(80,47,61, 0.1)",
          }}
        >
          <div className="flex items-center mb-4">
            <span
              style={{
                display: "inline-block",
                width: "2rem",
                height: "2rem",
                background: "#512f3d",
                color: "#fff",
                borderRadius: "999px",
                textAlign: "center",
                fontWeight: "bold",
                fontSize: "1.3rem",
                marginRight: "1rem",
              }}
            >
              {is403 ? "🔒" : isFetch ? "🌐" : "⚠️"}
            </span>
            <h1
              className="font-bold text-2xl"
              style={{ color: "#fff", margin: 0 }}
            >
              {is403 ? "Access Denied" : isFetch ? "Network Error" : "Error"}
            </h1>
          </div>
          <div
            style={{
              color: "#ff5151",
              fontWeight: "bold",
              marginBottom: "1.2rem",
              fontSize: "1.1rem",
            }}
          >
            {is403
              ? "You do not have permission to access this page. Please contact your administrator if you need access."
              : errorMessage}
          </div>
          <div style={{ color: "#fff", opacity: 0.7 }}>
            {is403
              ? "(Error 403: Forbidden)"
              : isFetch
                ? "(Could not reach backend server)"
                : statusCode
                  ? `(Error ${statusCode})`
                  : null}
          </div>
        </div>
      </div>
    );
  }

>>>>>>> e55f64a (added blockers and allow admin to add user)
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
<<<<<<< HEAD
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
=======
          {users
            .filter((user) => user.username !== currentUserEmail)
            .map((user) => (
              <tr key={user.username} className="border-b">
                <td className="p-3">{user.username}</td>
                <td className="p-3 capitalize">{user.role}</td>
                <td className="p-3">
                  <div className="flex flex-col md:flex-row gap-3 items-center">
                    {/* Show "Downgrade" ONLY if YOU are SUPER_ADMIN and THEY are admin */}
                    {currentUserRole === "SUPER_ADMIN" &&
                      user.role === "ADMIN" && (
                        <Button
                          onClick={() => openRoleUpdateDialog(user, "user")}
                          className="bg-yellow-600 hover:bg-yellow-700 text-white"
                        >
                          Downgrade to User
                        </Button>
                      )}

                    {/* Show "Upgrade" if YOU are ADMIN/SUPER_ADMIN and THEY are USER */}
                    {(currentUserRole === "SUPER_ADMIN" ||
                      currentUserRole === "ADMIN") &&
                      user.role === "USER" && (
                        <Button
                          onClick={() => openRoleUpdateDialog(user, "admin")}
                          className="bg-green-600 hover:bg-green-700 text-white"
                        >
                          Upgrade to Admin
                        </Button>
                      )}

                    {/* Show "Delete" if... */}
                    {
                      // A) YOU are SUPER_ADMIN (and they aren't)
                      ((currentUserRole === "SUPER_ADMIN" &&
                        user.role !== "SUPER_ADMIN") ||
                        // B) YOU are ADMIN and THEY are USER
                        (currentUserRole === "ADMIN" &&
                          user.role === "USER")) && (
                        <Button
                          onClick={() => openDeleteDialog(user)}
                          className="bg-red-600 hover:bg-red-700 text-white"
                        >
                          Delete
                        </Button>
                      )
                    }
>>>>>>> e55f64a (added blockers and allow admin to add user)
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
