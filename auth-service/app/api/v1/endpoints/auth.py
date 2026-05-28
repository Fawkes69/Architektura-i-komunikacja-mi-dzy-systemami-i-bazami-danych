from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordRequestForm
from jose import JWTError
from sqlalchemy.orm import Session

from app.core.security import create_access_token, create_refresh_token, decode_token
from app.db.session import get_db
from app.schemas.user import Token, TokenRefresh, TokenVerify, UserCreate, UserRead
from app.services.user_service import (
    authenticate_user,
    create_user,
    get_user_by_email,
    get_user_by_id,
)

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=UserRead, status_code=status.HTTP_201_CREATED)
def register(user_in: UserCreate, db: Session = Depends(get_db)):
    if get_user_by_email(db, user_in.email):
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Email already registered",
        )
    return create_user(db, user_in)


@router.post("/login", response_model=Token)
def login(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    user = authenticate_user(db, form_data.username, form_data.password)
    if not user:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    if not user.is_active:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Inactive user")

    data = {"sub": str(user.id)}
    return Token(
        access_token=create_access_token(data),
        refresh_token=create_refresh_token(data),
    )


@router.post("/refresh", response_model=Token)
def refresh_token(body: TokenRefresh, db: Session = Depends(get_db)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid refresh token"
    )
    try:
        payload = decode_token(body.refresh_token)
        if payload.get("type") != "refresh":
            raise credentials_exception
        user_id: int = int(payload["sub"])
    except (JWTError, KeyError, ValueError):
        raise credentials_exception

    user = get_user_by_id(db, user_id)
    if not user or not user.is_active:
        raise credentials_exception

    data = {"sub": str(user.id)}
    return Token(
        access_token=create_access_token(data),
        refresh_token=create_refresh_token(data),
    )


@router.post("/verify")
def verify_token(body: TokenVerify):
    """Internal endpoint: verify access token and return user_id."""
    try:
        payload = decode_token(body.token)
        if payload.get("type") != "access":
            raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Not an access token")
        return {"user_id": int(payload["sub"]), "valid": True}
    except JWTError:
        raise HTTPException(status_code=status.HTTP_401_UNAUTHORIZED, detail="Invalid token")
