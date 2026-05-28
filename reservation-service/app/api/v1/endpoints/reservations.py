from typing import List

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session

from app.core.deps import CurrentUser, get_current_user, require_admin
from app.db.session import get_db
from app.schemas.reservation import ReservationCreate, ReservationRead
from app.services.reservation_service import (
    cancel_reservation,
    create_reservation,
    get_reservation,
    list_all_reservations,
    list_user_reservations,
)

router = APIRouter(prefix="/reservations", tags=["reservations"])


@router.get("/", response_model=List[ReservationRead])
def get_reservations(
    db: Session = Depends(get_db),
    current_user: CurrentUser = Depends(get_current_user),
):
    if current_user.is_admin:
        return list_all_reservations(db)
    return list_user_reservations(db, current_user.user_id)


@router.post("/", response_model=ReservationRead, status_code=status.HTTP_201_CREATED)
def make_reservation(
    res_in: ReservationCreate,
    db: Session = Depends(get_db),
    current_user: CurrentUser = Depends(get_current_user),
):
    try:
        return create_reservation(db, current_user.user_id, res_in)
    except ValueError as e:
        raise HTTPException(status_code=status.HTTP_409_CONFLICT, detail=str(e))


@router.get("/{reservation_id}", response_model=ReservationRead)
def get_reservation_detail(
    reservation_id: int,
    db: Session = Depends(get_db),
    current_user: CurrentUser = Depends(get_current_user),
):
    res = get_reservation(db, reservation_id)
    if not res:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Reservation not found")
    if not current_user.is_admin and res.user_id != current_user.user_id:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail="Access denied")
    return res


@router.delete("/{reservation_id}", response_model=ReservationRead)
def cancel(
    reservation_id: int,
    db: Session = Depends(get_db),
    current_user: CurrentUser = Depends(get_current_user),
):
    res = get_reservation(db, reservation_id)
    if not res:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Reservation not found")
    try:
        return cancel_reservation(db, res, current_user.user_id, current_user.is_admin)
    except (PermissionError, ValueError) as e:
        raise HTTPException(status_code=status.HTTP_403_FORBIDDEN, detail=str(e))
