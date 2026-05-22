from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.endpoints import reservations, spaces
from app.db.session import Base, engine

Base.metadata.create_all(bind=engine)

app = FastAPI(
    title="CoWorking Reservation Service",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(spaces.router, prefix="/api/v1")
app.include_router(reservations.router, prefix="/api/v1")


@app.get("/health")
def health():
    return {"status": "ok", "service": "reservation"}
