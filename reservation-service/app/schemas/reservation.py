from datetime import datetime
from typing import Optional

from pydantic import BaseModel, field_validator

from app.models.space import ReservationStatus, SpaceType


# ---------- Space ----------

class SpaceCreate(BaseModel):
    name: str
    description: str = ""
    space_type: SpaceType
    floor: int
    capacity: int = 1
    pos_x: float = 0.0
    pos_y: float = 0.0


class SpaceUpdate(BaseModel):
    name: Optional[str] = None
    description: Optional[str] = None
    is_available: Optional[bool] = None
    capacity: Optional[int] = None
    pos_x: Optional[float] = None
    pos_y: Optional[float] = None


class SpaceRead(BaseModel):
    id: int
    name: str
    description: str
    space_type: SpaceType
    floor: int
    capacity: int
    is_available: bool
    pos_x: float
    pos_y: float
    created_at: datetime

    model_config = {"from_attributes": True}


# ---------- Reservation ----------

class ReservationCreate(BaseModel):
    space_id: int
    start_time: datetime
    end_time: datetime
    notes: str = ""

    @field_validator("end_time")
    @classmethod
    def end_after_start(cls, v: datetime, info) -> datetime:
        start = info.data.get("start_time")
        if start and v <= start:
            raise ValueError("end_time must be after start_time")
        return v


class ReservationRead(BaseModel):
    id: int
    user_id: int
    space_id: int
    start_time: datetime
    end_time: datetime
    status: ReservationStatus
    notes: str
    created_at: datetime
    space: Optional[SpaceRead] = None

    model_config = {"from_attributes": True}


class AvailabilityQuery(BaseModel):
    date: str           # YYYY-MM-DD
    floor: Optional[int] = None
    space_type: Optional[SpaceType] = None
