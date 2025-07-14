# Pokretanje monitornig sistema preko Docker-a

1. Idi u Start → kucaj cmd → desni klik → Run as Administrator
2. Uđi u folder monitoring
3. Pokreni komandu: `docker-compose up -d`. Ova komanda će pokrenuti potrebne servise u pozadini.

# Grafana Dashboard za monitoring

Ovaj folder sadrži eksportovani Grafana dashboard koji prikazuje:

- Broj aktivnih korisnika u toku 24h
- Prosečno zauzeće CPU u toku 24h
- Prosečno trajanje HTTP POST zahteva za kreiranje objava

## Kako koristiti

1. Otvori Grafana na `http://localhost:3000`
2. Idi na Dashboards → Import
3. Učitaj fajl `monitoring-dashboard.json`
4. Klikni Import

Prometheus mora biti povezan kao data source.
