from typing import List, Optional

from fastapi import APIRouter, Depends, HTTPException, Query, status
from sqlalchemy.orm import Session

from app.core.deps import CurrentUser, get_current_user, require_admin
from app.db.session import get_db
from app.models.space import SpaceType
from app.schemas.reservation import SpaceCreate, SpaceRead, SpaceUpdate
from app.services.reservation_service import get_available_spaces_for_date
from app.services.space_service import (
    create_space,
    delete_space,
    get_space,
    list_spaces,
    update_space,
)

router = APIRouter(prefix="/spaces", tags=["spaces"])


@router.get("/", response_model=List[SpaceRead])
def get_spaces(
    floor: Optional[int] = Query(None),
    space_type: Optional[SpaceType] = Query(None),
    date: Optional[str] = Query(None, description="YYYY-MM-DD filter available spaces"),
    db: Session = Depends(get_db),
    _: CurrentUser = Depends(get_current_user),
):
    spaces = list_spaces(db, floor=floor, space_type=space_type)
    if date:
        booked_ids = get_available_spaces_for_date(db, date)
        for space in spaces:
            space._is_booked_on_date = space.id in booked_ids
    return spaces


@router.get("/{space_id}", response_model=SpaceRead)
def get_space_detail(
    space_id: int,
    db: Session = Depends(get_db),
    _: CurrentUser = Depends(get_current_user),
):
    space = get_space(db, space_id)
    if not space:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return space


@router.post("/", response_model=SpaceRead, status_code=status.HTTP_201_CREATED)
def create_new_space(
    space_in: SpaceCreate,
    db: Session = Depends(get_db),
    _: CurrentUser = Depends(require_admin),
):
    return create_space(db, space_in)


@router.patch("/{space_id}", response_model=SpaceRead)
def update_space_detail(
    space_id: int,
    space_in: SpaceUpdate,
    db: Session = Depends(get_db),
    _: CurrentUser = Depends(require_admin),
):
    space = get_space(db, space_id)
    if not space:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    return update_space(db, space, space_in)


@router.delete("/{space_id}", status_code=status.HTTP_204_NO_CONTENT)
def delete_space_resource(
    space_id: int,
    db: Session = Depends(get_db),
    _: CurrentUser = Depends(require_admin),
):
    space = get_space(db, space_id)
    if not space:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Space not found")
    delete_space(db, space)
