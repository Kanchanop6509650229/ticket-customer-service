/**
 * Main JavaScript file for the Event Ticket System web client
 */

// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', function() {
    console.log('Event Ticket System web client loaded');
    
    // Initialize tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function(tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // Initialize popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(function(popoverTriggerEl) {
        return new bootstrap.Popover(popoverTriggerEl);
    });
    
    // Add fade-in animation to main content
    const mainContent = document.querySelector('main');
    if (mainContent) {
        mainContent.classList.add('fade-in');
    }
    
    // Handle form validation
    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', event => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });
    
    // Handle AJAX errors globally
    $(document).ajaxError(function(event, jqXHR, settings, thrownError) {
        console.error('AJAX Error:', thrownError);
        
        // Show error message if status code is not 0 (aborted request)
        if (jqXHR.status !== 0) {
            let errorMessage = 'An error occurred while processing your request.';
            
            if (jqXHR.responseJSON && jqXHR.responseJSON.message) {
                errorMessage = jqXHR.responseJSON.message;
            } else if (jqXHR.statusText) {
                errorMessage = jqXHR.statusText;
            }
            
            // Show error alert
            showAlert('danger', errorMessage);
        }
    });
    
    // Function to show alert message
    window.showAlert = function(type, message) {
        // Create alert element
        const alertEl = document.createElement('div');
        alertEl.className = `alert alert-${type} alert-dismissible fade show`;
        alertEl.setAttribute('role', 'alert');
        alertEl.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;
        
        // Add alert to the top of the main content
        const mainContent = document.querySelector('main');
        if (mainContent) {
            mainContent.insertBefore(alertEl, mainContent.firstChild);
            
            // Auto-dismiss after 5 seconds
            setTimeout(() => {
                const bsAlert = new bootstrap.Alert(alertEl);
                bsAlert.close();
            }, 5000);
        }
    };
});
