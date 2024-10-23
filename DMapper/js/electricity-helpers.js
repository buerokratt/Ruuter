// Helper function to handle date conversion and timezone adjustment
function convertToEEST(timestamp) {
    const date = new Date(timestamp * 1000); // Convert to milliseconds
    date.setHours(date.getHours() + 3); // Adjust for EEST (UTC+3)
    return date;
}

Handlebars.registerHelper('startDate', function(response) {
    const firstEntry = response[0]; 
    return convertToEEST(firstEntry.timestamp).toISOString().slice(0, 10); // Format YYYY-MM-DD
});

Handlebars.registerHelper('endDate', function(response) {
    const lastEntry = response[response.length - 1];
    return convertToEEST(lastEntry.timestamp).toISOString().slice(0, 10); // Format YYYY-MM-DD
});

Handlebars.registerHelper('formatTimestamp', function(timestamp) {
    return convertToEEST(timestamp).toISOString().slice(0, 16).replace('T', ' '); // Format "YYYY-MM-DD HH:mm"
});

Handlebars.registerHelper('addVAT', function(price) {
    const VAT = 1.22;
    const total = price * VAT;
    return Math.round(total * 100) / 100; // Round to 2 decimal places
});
