# WSID Frontend - ClojureScript re-frame Application

## Project Overview
A ClojureScript single-page application (SPA) for decision-making assistance using the re-frame architecture pattern. The app helps users model decisions through factors, scenarios, and weighted evaluations.

## Architecture
Built with the standard re-frame pattern:
- **Views**: UI components using Reagent (React wrapper)
- **Events**: State mutations and side effects
- **Subscriptions**: Derived state queries
- **Database**: Centralized app state with clojure.spec validation

## Core Domain
The application models decisions through:
- **Factors**: Weighted criteria (0-10 scale) for decision-making
- **Scenarios**: Potential outcomes or alternatives
- **Factor Values**: Numerical ratings of how scenarios perform on each factor
- **User**: Authentication state with JWT token storage

## Technology Stack
- **Framework**: re-frame + Reagent
- **Build Tool**: Shadow CLJS with hot reload
- **Styling**: Tailwind CSS v4 with live compilation
- **Development**: nREPL integration, re-frame-10x dev tools
- **Dependencies**: React 17, transit for data serialization

## Key Features
- Factor-based decision modeling with weighted scoring
- User authentication and session management
- Local storage persistence for transient state
- Modal forms for data entry with validation
- Real-time development with hot reload

## Project Structure
- `src/wsid/core.cljs` - Application initialization and mounting
- `src/wsid/db.cljs` - App state schema and validation specs
- `src/wsid/events/` - Event handlers for state mutations
- `src/wsid/views/` - UI components and forms
- `src/wsid/subs/` - Subscription queries for derived state

## Development Workflow
- `npm run watch` - Development server with hot reload
- `npm run repl` - ClojureScript nREPL connection
- `npm run release` - Production build