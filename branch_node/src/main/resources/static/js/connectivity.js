document.addEventListener("DOMContentLoaded", function () {
    const indicator = document.getElementById("conn-indicator");
    const text = document.getElementById("conn-text");
    const syncStats = document.getElementById("sync-stats");

    function checkHealth() {
        // Polling del endpoint ConnectivityService (Ping/Echo tactic C006.1.3)
        fetch('/api/health')
            .then(response => {
                if (!response.ok) throw new Error("Network response error");
                return response.json();
            })
            .then(data => {
                if (data.online) {
                    indicator.className = "connectivity-indicator online";
                    text.innerText = "Conectado al Hub";
                } else {
                    indicator.className = "connectivity-indicator offline";
                    text.innerText = "Offline (Local)";
                }
                if(syncStats) {
                    syncStats.innerText = `Pendientes: ${data.pending_syncs} | Conflictos: ${data.conflicts}`;
                }
            })
            .catch(error => {
                // Si el Branch Node local está caído
                indicator.className = "connectivity-indicator offline";
                text.innerText = "Servidor Local Caído";
            });
    }

    // Revisar conectividad cada 15 segundos
    setInterval(checkHealth, 15000);
    checkHealth(); // Ejecutar inmediatamente
});
