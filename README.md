# Media Collection REST Webservice

A self-hosted RESTful web service for managing your personal media collection — built in Java using the custom forestJ framework.  
Designed to work seamlessly with the Media Collection Android App, this backend enables you to keep full control over your media data by hosting it on your own server.  

*net.forestany.mediacollectionrest.webservice*

---

## Features

- RESTful API with clean and consistent endpoints for all CRUD operations  
- Self-hosted design — your data stays entirely under your control  
- Secure communication with TLS 1.3 and AES-encrypted HTTP Authentication  
- Lightweight and efficient — powered by the forestJ framework, optimized for performance and simplicity  
- Fully compatible with the companion Android app "Media Collection"

---

## Privacy First

Unlike cloud-based services, Media Collection REST Webservice never stores your media information on third-party servers.  
You can deploy it on any server or even on a local machine to keep your library completely private and secure.

---

## Tech Stack

- Language: Java  
- Framework: [forestJ](https://github.com/ReneArentz/forestJ)  
- Protocol: HTTPS (TLS 1.3)  
- Encryption: AES (for HTTP AUTH)  
- Client: Media Collection Android App  

---

## Companion App

The **Media Collection Android App** connects directly to this REST service, allowing you to browse, edit, and manage your media library on the go.

[net.forestany.mediacollection](https://github.com/ReneArentz/net.forestany.mediacollection)

---

## License

This project is open source under the GNU GPL v3 license — feel free to host, modify, and improve it while maintaining attribution.
