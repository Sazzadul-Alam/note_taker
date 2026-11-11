# Deployment Guide: Backend on Render with Docker

## Option 1: Direct Web Service (via Git repo)
1. Push your repository to GitHub / GitLab.
2. In [Render Dashboard](https://dashboard.render.com), click **New +** -> **Web Service**.
3. Connect your repository.
4. Set the following configuration:
   - **Root Directory**: `backend`
   - **Environment / Runtime**: `Docker`
   - **Dockerfile Path**: `Dockerfile` (or `./Dockerfile`)
   - **Plan**: Free (or preferred plan)

5. **Environment Variables** (Optional, defaults already set in code):
   - `PORT`: `8080` (or let Render assign automatically, Spring Boot will bind to it)
   - `MONGODB_URI`: `<your-mongodb-atlas-uri>` (already has default fallback in `application.properties`)
   - `JWT_SECRET`: `<your-jwt-secret-key>`
   - `JWT_EXPIRATION`: `86400000`

---

## Option 2: render.yaml (Blueprint)
You can also deploy via Render Blueprint using the `render.yaml` specification in this project.

