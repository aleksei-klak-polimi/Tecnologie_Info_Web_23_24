-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema ImageManagerDB
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema ImageManagerDB
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ImageManagerDB` DEFAULT CHARACTER SET utf8 ;
USE `ImageManagerDB` ;

-- -----------------------------------------------------
-- Table `ImageManagerDB`.`User`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ImageManagerDB`.`User` (
  `id` INT NOT NULL,
  `username` VARCHAR(45) NOT NULL,
  `email` VARCHAR(45) NOT NULL,
  `password` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `username_UNIQUE` (`username` ASC) VISIBLE,
  UNIQUE INDEX `email_UNIQUE` (`email` ASC) VISIBLE)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ImageManagerDB`.`Picture`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ImageManagerDB`.`Picture` (
  `id` INT NOT NULL,
  `path` VARCHAR(512) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `description` VARCHAR(1024) NULL,
  `uploadDate` DATE NOT NULL,
  `uploader` INT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `path_UNIQUE` (`path` ASC) VISIBLE,
  INDEX `fk_Pictures_owner_idx` (`uploader` ASC) VISIBLE,
  CONSTRAINT `fk_Pictures_owner`
    FOREIGN KEY (`uploader`)
    REFERENCES `ImageManagerDB`.`User` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ImageManagerDB`.`Comment`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ImageManagerDB`.`Comment` (
  `id` INT NOT NULL,
  `body` VARCHAR(1024) NOT NULL,
  `pictureId` INT NOT NULL,
  `author` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Comments_Picture_idx` (`pictureId` ASC) VISIBLE,
  INDEX `fk_Comments_Author_idx` (`author` ASC) VISIBLE,
  CONSTRAINT `fk_Comments_Picture`
    FOREIGN KEY (`pictureId`)
    REFERENCES `ImageManagerDB`.`Picture` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Comments_Author`
    FOREIGN KEY (`author`)
    REFERENCES `ImageManagerDB`.`User` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ImageManagerDB`.`Album`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ImageManagerDB`.`Album` (
  `id` VARCHAR(45) NOT NULL,
  `title` VARCHAR(128) NOT NULL,
  `owner` INT NOT NULL,
  `creationDate` DATE NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `fk_Albums_Owner_idx` (`owner` ASC) VISIBLE,
  CONSTRAINT `fk_Albums_Owner`
    FOREIGN KEY (`owner`)
    REFERENCES `ImageManagerDB`.`User` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `ImageManagerDB`.`Album_Picture`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ImageManagerDB`.`Album_Picture` (
  `pictureId` INT NOT NULL,
  `albumId` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`pictureId`, `albumId`),
  INDEX `fk_Album_Picture_2_idx` (`albumId` ASC) VISIBLE,
  CONSTRAINT `fk_Album_Picture_1`
    FOREIGN KEY (`pictureId`)
    REFERENCES `ImageManagerDB`.`Picture` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_Album_Picture_2`
    FOREIGN KEY (`albumId`)
    REFERENCES `ImageManagerDB`.`Album` (`id`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
