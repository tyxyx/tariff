"use client";

import { Button } from "@/components/ui/button";
import { colors } from "@/styles/colors";

export function ConfirmDialog({
  title,
  message,
  onConfirm,
  onCancel,
  confirmText = "Confirm",
  confirmColor = "bg-red-600",
}) {
  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
      <div
        className="p-6 rounded-lg shadow-lg w-full max-w-sm"
        style={{ backgroundColor: colors.card, color: colors.text }}
      >
        <h2 className="text-lg font-bold mb-4">{title}</h2>
        <p
          className="text-sm text-muted-foreground mb-6"
          dangerouslySetInnerHTML={{
            __html: message.replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>"),
          }}
        />

        <div className="flex justify-end gap-3">
          <Button
            onClick={onCancel}
            className="bg-gray-500 hover:bg-gray-600 text-white"
          >
            Cancel
          </Button>
          <Button
            onClick={onConfirm}
            className={`${confirmColor} hover:${confirmColor.replace("600", "700")} text-white`}
          >
            {confirmText}
          </Button>
        </div>
      </div>
    </div>
  );
}
