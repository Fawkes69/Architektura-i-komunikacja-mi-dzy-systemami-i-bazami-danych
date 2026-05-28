from pathlib import Path

from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    DATABASE_URL: str
    SECRET_KEY: str
    ALGORITHM: str = "HS256"
    AUTH_SERVICE_URL: str = "http://auth_service:8000"

    class Config:
        env_file = Path(__file__).resolve().parents[2] / ".env"


settings = Settings()
