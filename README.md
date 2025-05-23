# Cart Service

[![CI Workflow](https://github.com/PhumlaniDev/infinity-tech-cart-service/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/PhumlaniDev/infinity-tech-cart-service/actions/workflows/ci-cd.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=bugs)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Code Smells](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=code_smells)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=coverage)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Duplicated Lines (%)](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=duplicated_lines_density)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=PhumlaniDev_infinity-tech-cart-service&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=PhumlaniDev_infinity-tech-cart-service)

## Overview

The `cart-service` is a Spring Boot-based microservice responsible for managing shopping cart operations. It integrates with Redis for caching and provides RESTful APIs for cart management.

## Features

- Shopping cart management (add, update, remove items).
- Integration with Redis for caching.
- Integration with SonarCloud for code quality analysis.
- Dockerized for easy deployment.
- CI/CD pipeline with GitHub Actions.
- Static and dynamic security testing workflows.

## Prerequisites

- Java 21
- Maven
- Docker
- Redis
- GitHub account with necessary secrets configured for CI/CD.

## Getting Started

### Clone the Repository

```bash
git clone https://github.com/PhumlaniDev/infinity-tech-cart-service.git
cd infinity-tech-cart-service