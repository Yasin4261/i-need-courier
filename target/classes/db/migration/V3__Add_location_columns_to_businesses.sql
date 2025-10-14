-- V3__Add_location_columns_to_businesses.sql
-- Add missing latitude and longitude columns to the businesses table

ALTER TABLE businesses
ADD COLUMN latitude FLOAT,
ADD COLUMN longitude FLOAT;
