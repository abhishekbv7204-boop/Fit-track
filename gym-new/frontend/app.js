const loginScreen = document.getElementById("login-screen");
const appScreen = document.getElementById("app-screen");
const loginForm = document.getElementById("login-form");
const loginMessage = document.getElementById("login-message");
const logoutButton = document.getElementById("logout-button");
const addMemberButton = document.getElementById("add-member-button");
const memberDialog = document.getElementById("member-dialog");
const memberForm = document.getElementById("member-form");
const cancelDialog = document.getElementById("cancel-dialog");
const membersBody = document.getElementById("members-body");
const memberSearch = document.getElementById("member-search");
const plansList = document.getElementById("plans-list");
const memberPlan = document.getElementById("member-plan");
const dialogTitle = document.getElementById("dialog-title");
const databaseTablesContainer = document.getElementById("database-tables");
const navButtons = document.querySelectorAll(".nav-button");
const tupleDialog = document.getElementById("tuple-dialog");
const tupleForm = document.getElementById("tuple-form");
const tupleDialogTitle = document.getElementById("tuple-dialog-title");
const tupleDialogNote = document.getElementById("tuple-dialog-note");
const tupleFields = document.getElementById("tuple-fields");
const cancelTupleDialog = document.getElementById("cancel-tuple-dialog");

let members = [];
let plans = [];
let databaseTables = [];
let activeTupleTable = null;

navButtons.forEach((button) => {
    button.addEventListener("click", () => {
        navButtons.forEach((item) => item.classList.remove("active"));
        button.classList.add("active");

        const target = document.getElementById(`${button.dataset.view}-view`);
        if (target) {
            target.scrollIntoView({ behavior: "smooth", block: "start" });
        }
    });
});

loginForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    loginMessage.textContent = "";

    const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        })
    });

    const result = await response.json();
    if (!result.success) {
        loginMessage.textContent = result.message || "Login failed";
        return;
    }

    loginScreen.classList.add("hidden");
    appScreen.classList.remove("hidden");
    await loadData();
});

logoutButton.addEventListener("click", () => {
    appScreen.classList.add("hidden");
    loginScreen.classList.remove("hidden");
});

addMemberButton.addEventListener("click", () => {
    openMemberDialog();
});

cancelDialog.addEventListener("click", () => {
    memberDialog.close();
});

cancelTupleDialog.addEventListener("click", () => {
    tupleDialog.close();
});

memberSearch.addEventListener("input", () => {
    renderMembers();
});

memberForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const name = document.getElementById("member-name").value.trim();
    if (!/^[A-Za-z ]+$/.test(name)) {
        alert("Name must contain only letters and spaces.");
        return;
    }

    const id = document.getElementById("member-id").value;
    const payload = getFormPayload();
    const isEditing = Boolean(id);

    if (isEditing) {
        payload.id = id;
    }

    const response = await fetch("/api/members", {
        method: isEditing ? "PUT" : "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    const result = await response.json();
    if (!result.success) {
        alert(result.message || "Could not save member");
        return;
    }

    memberDialog.close();
    await loadData();
});

membersBody.addEventListener("click", async (event) => {
    const button = event.target.closest("button");
    if (!button) return;

    const id = Number(button.dataset.id);
    const member = members.find((item) => item.id === id);

    if (button.classList.contains("edit-button")) {
        openMemberDialog(member);
    }

    if (button.classList.contains("delete-button")) {
        const confirmed = confirm(`Delete ${member.name}?`);
        if (!confirmed) return;

        const response = await fetch(`/api/members?id=${id}`, {
            method: "DELETE"
        });
        const result = await response.json();

        if (!result.success) {
            alert("Could not delete member");
            return;
        }

        await loadData();
    }
});

databaseTablesContainer.addEventListener("click", async (event) => {
    const addButton = event.target.closest(".add-tuple-button");
    if (addButton) {
        const table = databaseTables.find((item) => item.name === addButton.dataset.table);
        if (table) {
            openTupleDialog(table);
        }
        return;
    }

    const deleteButton = event.target.closest(".delete-tuple-button");
    if (deleteButton) {
        const table = deleteButton.dataset.table;
        const column = deleteButton.dataset.column;
        const value = deleteButton.dataset.value;

        const confirmed = confirm(`Delete row with ${column} = ${value} from ${table}?`);
        if (!confirmed) return;

        const response = await fetch(`/api/database?table=${encodeURIComponent(table)}&column=${encodeURIComponent(column)}&value=${encodeURIComponent(value)}`, {
            method: "DELETE"
        });

        const result = await response.json();
        if (!result.success) {
            alert(result.message || "Could not delete row. It may be referenced by another table.");
            return;
        }

        await loadData();
    }
});

tupleForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const payload = {};
    const inputs = tupleFields.querySelectorAll("input");

    for (const input of inputs) {
        const value = input.value.trim();
        if (!value) continue;

        if ((input.name === "name" || input.name.endsWith("_name")) && !isValidName(value)) {
            alert("Name fields must contain only letters and spaces.");
            return;
        }

        if ((input.name === "gender" || input.name.endsWith("_gender")) && !isValidGender(value)) {
            alert("Gender fields must be Male, Female, or Other.");
            return;
        }

        payload[input.name] = value;
    }

    const response = await fetch(`/api/database?table=${encodeURIComponent(activeTupleTable.name)}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
    });

    const result = await response.json();
    if (!result.success) {
        alert(result.message || "Could not insert tuple");
        return;
    }

    tupleDialog.close();
    await loadData();
});

async function loadData() {
    const [membersResponse, plansResponse, databaseResponse] = await Promise.all([
        fetch("/api/members"),
        fetch("/api/plans"),
        fetch("/api/database")
    ]);

    members = await membersResponse.json();
    plans = await plansResponse.json();
    databaseTables = await databaseResponse.json();

    renderPlanOptions();
    renderMembers();
    renderPlans();
    renderDatabaseTables();
    renderStats();
}

function renderMembers() {
    const search = memberSearch.value.trim().toLowerCase();
    const visibleMembers = members.filter((member) =>
        member.name.toLowerCase().includes(search) ||
        member.phone.toLowerCase().includes(search) ||
        member.email.toLowerCase().includes(search)
    );

    membersBody.innerHTML = visibleMembers.map((member) => `
        <tr>
            <td>${member.id}</td>
            <td>${escapeHtml(member.name)}</td>
            <td>${member.age}</td>
            <td>${escapeHtml(member.phone)}</td>
            <td>${escapeHtml(member.email)}</td>
            <td>${escapeHtml(member.planName)}</td>
            <td>${escapeHtml(member.joinDate)}</td>
            <td>
                <div class="row-actions">
                    <button class="edit-button" data-id="${member.id}">Edit</button>
                    <button class="delete-button" data-id="${member.id}">Delete</button>
                </div>
            </td>
        </tr>
    `).join("");
}

function renderPlans() {
    plansList.innerHTML = plans.map((plan) => `
        <article>
            <h3>${escapeHtml(plan.name)}</h3>
            <p>${plan.durationMonths} month(s)</p>
            <p>Rs. ${plan.price}</p>
        </article>
    `).join("");
}

function renderPlanOptions() {
    memberPlan.innerHTML = plans.map((plan) => `
        <option value="${plan.id}">${escapeHtml(plan.name)} - Rs. ${plan.price}</option>
    `).join("");
}

function renderStats() {
    document.getElementById("total-members").textContent = members.length;
    document.getElementById("total-plans").textContent = plans.length;
    document.getElementById("latest-member").textContent = members[0]?.name || "None";
    document.getElementById("total-trainers").textContent = tableCount("trainers");
    document.getElementById("total-payments").textContent = tableCount("payments");
    document.getElementById("total-attendance").textContent = tableCount("attendance");
}

function renderDatabaseTables() {
    databaseTablesContainer.innerHTML = databaseTables.map((table) => `
        <article class="database-card">
            <div class="database-card-header">
                <div>
                    <h3>${formatTableName(table.name)}</h3>
                    <span>${table.rows.length} tuple(s)</span>
                </div>
                <button class="add-tuple-button" data-table="${table.name}">Add Tuple</button>
            </div>
            <div class="table-wrap">
                <table>
                    <thead>
                        <tr>
                            ${table.columns.map((column) => `<th>${escapeHtml(column)}</th>`).join("")}
                            <th>Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${renderDatabaseRows(table)}
                    </tbody>
                </table>
            </div>
        </article>
    `).join("");
}

function renderDatabaseRows(table) {
    if (table.rows.length === 0) {
        return `
            <tr>
                <td colspan="${table.columns.length + 1}">No tuples found in this table.</td>
            </tr>
        `;
    }

    const idColumn = table.columns.find((col) => col === "id" || col.endsWith("_id"));

    return table.rows.map((row) => {
        const idValue = idColumn ? row[idColumn] : null;
        const deleteButton = idColumn && idValue ? `
            <button class="delete-tuple-button" data-table="${table.name}" data-column="${idColumn}" data-value="${idValue}">Delete</button>
        ` : "";

        return `
            <tr>
                ${table.columns.map((column) => `<td>${escapeHtml(row[column])}</td>`).join("")}
                <td>
                    <div class="row-actions">
                        ${deleteButton}
                    </div>
                </td>
            </tr>
        `;
    }).join("");
}

function tableCount(tableName) {
    return databaseTables.find((table) => table.name === tableName)?.rows.length || 0;
}

function formatTableName(tableName) {
    return tableName
        .split("_")
        .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
        .join(" ");
}

function openTupleDialog(table) {
    activeTupleTable = table;
    tupleForm.reset();
    tupleDialogTitle.textContent = `Add Tuple to ${formatTableName(table.name)}`;
    tupleDialogNote.textContent = table.name === "payments" || table.name === "attendance" || table.name === "workout_schedule"
        ? "Use existing member_id and trainer_id values from the related tables."
        : "Leave auto-generated id fields blank when available.";

    tupleFields.innerHTML = table.columns.map((column) => {
        const inputType = inputTypeForColumn(column);
        const autoText = isAutoGeneratedColumn(table.name, column) ? "Auto generated if blank" : "";
        const dateValue = inputType === "date" ? `value="${today()}"` : "";
        const isName = isNameField(column);
        const isGender = isGenderField(column);
        const pattern = isName ? 'pattern="[A-Za-z ]+"' : isGender ? 'pattern="^(Male|Female|Other)$"' : '';
        const title = isName ? 'title="Name must contain only letters and spaces"' : isGender ? 'title="Gender must be Male, Female, or Other"' : '';

        return `
            <label for="tuple-${column}">${escapeHtml(column)}</label>
            <input
                id="tuple-${column}"
                name="${escapeHtml(column)}"
                type="${inputType}"
                placeholder="${autoText}"
                ${dateValue}
                ${pattern}
                ${title}
            >
        `;
    }).join("");

    tupleDialog.showModal();
}

function inputTypeForColumn(column) {
    if (column.includes("date")) return "date";
    if (column === "password") return "password";
    if (
        column === "id" ||
        column.endsWith("_id") ||
        column.includes("amount") ||
        column.includes("price") ||
        column.includes("age") ||
        column.includes("duration") ||
        column.includes("experience")
    ) {
        return "number";
    }
    return "text";
}

function isNameField(column) {
    if (!column) return false;
    const lower = column.toLowerCase();
    if (lower === "name") return true;
    if (lower.endsWith("_name")) return true;
    if (column.endsWith("Name") && !lower.endsWith("username")) return true;
    return false;
}

function isAutoGeneratedColumn(tableName, column) {
    return (tableName === "members" && column === "id") ||
        (tableName === "users" && column === "id");
}

function today() {
    return new Date().toISOString().slice(0, 10);
}

function openMemberDialog(member = null) {
    memberForm.reset();
    dialogTitle.textContent = member ? "Edit Member" : "Add Member";

    document.getElementById("member-id").value = member?.id || "";
    document.getElementById("member-name").value = member?.name || "";
    document.getElementById("member-age").value = member?.age || "";
    document.getElementById("member-gender").value = member?.gender || "Male";
    document.getElementById("member-phone").value = member?.phone || "";
    document.getElementById("member-email").value = member?.email || "";
    document.getElementById("member-plan").value = member?.planId || plans[0]?.id || "";

    memberDialog.showModal();
}

function getFormPayload() {
    return {
        name: document.getElementById("member-name").value.trim(),
        age: document.getElementById("member-age").value,
        gender: document.getElementById("member-gender").value,
        phone: document.getElementById("member-phone").value.trim(),
        email: document.getElementById("member-email").value.trim(),
        planId: document.getElementById("member-plan").value
    };
}

function isValidName(value) {
    return /^[A-Za-z ]+$/.test(value);
}

function isGenderField(column) {
    if (!column) return false;
    const lower = column.toLowerCase();
    return lower === "gender" || lower.endsWith("_gender") || column.endsWith("Gender");
}

function isValidGender(value) {
    if (!value) return false;
    const normalized = value.trim().toLowerCase();
    return normalized === "male" || normalized === "female" || normalized === "other";
}

function escapeHtml(value) {
    return String(value ?? "")
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#039;");
}
