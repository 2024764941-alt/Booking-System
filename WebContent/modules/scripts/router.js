// ROUTER MODULE
// Handles page navigation and state management

class PageRouter {
    constructor() {
        this.currentPage = 'landing';
        this.currentDashboardSection = 'overview';
    }

    // Show a page by ID
    showPage(pageId) {
        document.querySelectorAll('.page').forEach(page => {
            page.classList.remove('active');
        });
        
        const targetPage = document.getElementById(pageId);
        if (targetPage) {
            targetPage.classList.add('active');
            this.currentPage = pageId;
        } else {
            console.error(`Page ${pageId} not found`);
        }
    }

    // Show a dashboard section
    showDashboardSection(sectionId) {
        document.querySelectorAll('.dashboard-section').forEach(section => {
            section.classList.remove('active');
        });
        
        const targetSection = document.getElementById(sectionId);
        if (targetSection) {
            targetSection.classList.add('active');
            this.currentDashboardSection = sectionId;
        } else {
            console.error(`Dashboard section ${sectionId} not found`);
        }
    }

    // Get current page
    getCurrentPage() {
        return this.currentPage;
    }

    // Get current dashboard section
    getCurrentDashboardSection() {
        return this.currentDashboardSection;
    }
}

// Export for use in other modules
const router = new PageRouter();