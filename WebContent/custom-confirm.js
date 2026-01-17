// Custom Confirm Dialog Component
function showConfirmDialog(message, onConfirm, onCancel) {
    // Remove any existing confirm dialog
    const existingDialog = document.getElementById('custom-confirm-dialog');
    if (existingDialog) {
        existingDialog.remove();
    }

    // Create dialog HTML
    const dialogHTML = `
        <div id="custom-confirm-dialog" style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0, 0, 0, 0.5);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 10000;
            animation: fadeIn 0.2s ease;
        ">
            <div style="
                background: white;
                border-radius: 12px;
                padding: 24px;
                max-width: 400px;
                width: 90%;
                box-shadow: 0 10px 40px rgba(0,0,0,0.2);
                animation: slideUp 0.3s ease;
            ">
                <div style="
                    font-size: 18px;
                    font-weight: 600;
                    color: #333;
                    margin-bottom: 16px;
                    text-align: center;
                ">${message}</div>
                <div style="
                    display: flex;
                    gap: 12px;
                    justify-content: center;
                    margin-top: 24px;
                ">
                    <button id="confirm-cancel-btn" style="
                        padding: 10px 24px;
                        border: 1px solid #ddd;
                        background: white;
                        color: #666;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        cursor: pointer;
                        transition: all 0.2s;
                    ">Cancel</button>
                    <button id="confirm-ok-btn" style="
                        padding: 10px 24px;
                        border: none;
                        background: #c6a87c;
                        color: white;
                        border-radius: 6px;
                        font-size: 14px;
                        font-weight: 500;
                        cursor: pointer;
                        transition: all 0.2s;
                    ">OK</button>
                </div>
            </div>
        </div>
        <style>
            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
            @keyframes slideUp {
                from { transform: translateY(20px); opacity: 0; }
                to { transform: translateY(0); opacity: 1; }
            }
            #confirm-ok-btn:hover {
                background: #b39567;
                transform: translateY(-1px);
            }
            #confirm-cancel-btn:hover {
                background: #f5f5f5;
                border-color: #bbb;
            }
        </style>
    `;

    // Insert into DOM
    document.body.insertAdjacentHTML('beforeend', dialogHTML);

    const dialog = document.getElementById('custom-confirm-dialog');
    const okBtn = document.getElementById('confirm-ok-btn');
    const cancelBtn = document.getElementById('confirm-cancel-btn');

    // Handle OK
    okBtn.onclick = () => {
        dialog.remove();
        if (onConfirm) onConfirm();
    };

    // Handle Cancel
    cancelBtn.onclick = () => {
        dialog.remove();
        if (onCancel) onCancel();
    };

    // Handle background click
    dialog.onclick = (e) => {
        if (e.target === dialog) {
            dialog.remove();
            if (onCancel) onCancel();
        }
    };
}
