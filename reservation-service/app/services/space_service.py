from typing import List, Optional

from sqlalchemy.orm import Session

from app.models.space import Space, SpaceType
from app.schemas.reservation import SpaceCreate, SpaceUpdate


def get_space(db: Session, space_id: int) -> Optional[Space]:
    return db.query(Space).filter(Space.id == space_id).first()


def list_spaces(
    db: Session,
    floor: Optional[int] = None,
    space_type: Optional[SpaceType] = None,
    only_available: bool = False,
) -> List[Space]:
    q = db.query(Space)
    if floor is not None:
        q = q.filter(Space.floor == floor)
    if space_type is not None:
        q = q.filter(Space.space_type == space_type)
    if only_available:
        q = q.filter(Space.is_available == True)
    return q.all()


def create_space(db: Session, space_in: SpaceCreate) -> Space:
    space = Space(**space_in.model_dump())
    db.add(space)
    db.commit()
    db.refresh(space)
    return space


def update_space(db: Session, space: Space, space_in: SpaceUpdate) -> Space:
    for field, value in space_in.model_dump(exclude_none=True).items():
        setattr(space, field, value)
    db.commit()
    db.refresh(space)
    return space


def delete_space(db: Session, space: Space) -> None:
    db.delete(space)
    db.commit()
