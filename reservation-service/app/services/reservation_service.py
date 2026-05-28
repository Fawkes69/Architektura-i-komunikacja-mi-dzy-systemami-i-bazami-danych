from datetime import datetime, timezone
from typing import List, Optional

from sqlalchemy.orm import Session

from app.models.space import Reservation, ReservationStatus
from app.schemas.reservation import ReservationCreate


def _has_conflict(db: Session, space_id: int, start: datetime, end: datetime, exclude_id: Optional[int] = None) -> bool:
    q = (
        db.query(Reservation)
        .filter(
            Reservation.space_id == space_id,
            Reservation.status == ReservationStatus.ACTIVE,
            Reservation.start_time < end,
            Reservation.end_time > start,
        )
    )
    if exclude_id:
        q = q.filter(Reservation.id != exclude_id)
    return q.first() is not None


def create_reservation(db: Session, user_id: int, res_in: ReservationCreate) -> Reservation:
    if _has_conflict(db, res_in.space_id, res_in.start_time, res_in.end_time):
        raise ValueError("Space is already reserved for this time slot")

    reservation = Reservation(
        user_id=user_id,
        space_id=res_in.space_id,
        start_time=res_in.start_time,
        end_time=res_in.end_time,
        notes=res_in.notes,
    )
    db.add(reservation)
    db.commit()
    db.refresh(reservation)
    return reservation


def get_reservation(db: Session, reservation_id: int) -> Optional[Reservation]:
    return db.query(Reservation).filter(Reservation.id == reservation_id).first()


def list_user_reservations(db: Session, user_id: int) -> List[Reservation]:
    return (
        db.query(Reservation)
        .filter(Reservation.user_id == user_id)
        .order_by(Reservation.start_time.desc())
        .all()
    )


def list_all_reservations(db: Session) -> List[Reservation]:
    return db.query(Reservation).order_by(Reservation.start_time.desc()).all()


def cancel_reservation(db: Session, reservation: Reservation, user_id: int, is_admin: bool) -> Reservation:
    if not is_admin and reservation.user_id != user_id:
        raise PermissionError("You can only cancel your own reservations")
    if reservation.status == ReservationStatus.CANCELLED:
        raise ValueError("Reservation is already cancelled")
    reservation.status = ReservationStatus.CANCELLED
    db.commit()
    db.refresh(reservation)
    return reservation


def get_available_spaces_for_date(db: Session, date_str: str):
    """Return space IDs that have any active reservation on given date."""
    from datetime import date
    target = date.fromisoformat(date_str)
    day_start = datetime(target.year, target.month, target.day, 0, 0, 0, tzinfo=timezone.utc)
    day_end = datetime(target.year, target.month, target.day, 23, 59, 59, tzinfo=timezone.utc)

    booked_ids = (
        db.query(Reservation.space_id)
        .filter(
            Reservation.status == ReservationStatus.ACTIVE,
            Reservation.start_time < day_end,
            Reservation.end_time > day_start,
        )
        .distinct()
        .all()
    )
    return {r[0] for r in booked_ids}
